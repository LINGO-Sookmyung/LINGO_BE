package Sookmyung.Lingo.domains.translatedDocument.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import Sookmyung.Lingo.domains.enums.DocumentType;
import Sookmyung.Lingo.domains.enums.Language;
import Sookmyung.Lingo.domains.rawDocument.domain.RawDocument;
import Sookmyung.Lingo.domains.s3.service.S3Service;
import Sookmyung.Lingo.domains.translatedDocument.domain.TranslatedDocument;
import Sookmyung.Lingo.domains.translatedDocument.dto.TranslatedDocumentResponseDTO;
import Sookmyung.Lingo.domains.translatedDocument.dto.TranslatedDocumentResultDTO;
import Sookmyung.Lingo.domains.translatedDocument.repository.TranslatedDocumentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TranslatedDocumentService {

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;
	private final TranslatedDocumentRepository translatedDocumentRepository;
	private final S3Service s3Service;
	private final WebClient fastApiWebClient;

	@Autowired
	public TranslatedDocumentService(
		TranslatedDocumentRepository translatedDocumentRepository,
		S3Service s3Service,
		@Qualifier("fastApiWebClient") WebClient fastApiWebClient
	) {
		this.translatedDocumentRepository = translatedDocumentRepository;
		this.s3Service = s3Service;
		this.fastApiWebClient = fastApiWebClient;
	}

	private String toS3Uri(String bucket, String key) {
		return "s3://" + bucket + "/" + key;
	}

	@Transactional
	public TranslatedDocumentResultDTO translateAndSave(RawDocument rawDocument) {

		// 1) 원본 이미지들: pageNumber 순으로 정렬 + S3 URI로 변환
		List<String> s3Uris = rawDocument.getRawDocumentImages().stream()
			.sorted(Comparator.comparingLong(img -> img.getPageNumber()))
			.map(img -> toS3Uri(bucket, img.getRawFilePath()))  // <- 여기!
			.toList();

		//fastAPI 호출
		String docType = mapDocTypeForFastApi(rawDocument.getDocumentType());

		//binarize-and-ocr-multi
		String ocrOrGptJsonPath = callBinarizeAndOcrMulti(s3Uris, docType);

		//translate
		String lang = mapLanguageForFastApi(rawDocument.getLanguage());
		String translatedJsonPath = callTranslate(ocrOrGptJsonPath, lang);

		//generate-doc
		byte[] docxBytes = callGenerateDoc(docType, translatedJsonPath, /*ocr_path*/ "", lang);

		//S3 업로드
		String resultS3Key = s3Service.uploadTranslatedDocx(docxBytes);

		TranslatedDocument translated = TranslatedDocument.builder()
			.translatedDocumentName(buildDocNameForDb(rawDocument))
			.translatedFilePath(resultS3Key)
			.build();
		translated.setRawDocument(rawDocument);
		translatedDocumentRepository.save(translated);

		String downloadUrl = s3Service.getPresignedUrlToDownload(resultS3Key).getPath();

		return TranslatedDocumentResultDTO.builder()
			.rawDocumentId(rawDocument.getId())
			.translatedDocumentId(translated.getId())
			.translatedName(translated.getTranslatedDocumentName())
			.resultS3Key(resultS3Key)
			.presignedDownloadUrl(downloadUrl)
			.contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
			.build();
	}


	//이진화
	private String callBinarizeAndOcrMulti(List<String> imagePaths, String docType) {
		var req = Map.of("image_paths", imagePaths, "doc_type", docType);

		Map<String, Object> res = fastApiWebClient.post()
			.uri("/binarize-and-ocr-multi")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(req)
			.retrieve()
			.bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
			.block();

		if (res == null || res.get("path") == null) {
			throw new IllegalStateException("FastAPI 응답에 path가 없습니다.");
		}
		return String.valueOf(res.get("path"));
	}
	//번역
	private String callTranslate(String jsonPath, String lang) {
		var req = Map.of("json_path", jsonPath, "lang", lang);

		Map<String, Object> res = fastApiWebClient.post()
			.uri("/translate")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(req)
			.retrieve()
			.bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
			.block();

		if (res == null || res.get("path") == null) {
			throw new IllegalStateException("FastAPI 응답에 path가 없습니다.");
		}
		return String.valueOf(res.get("path"));
	}

	//문서 생성
	private byte[] callGenerateDoc(String docType, String jsonPath, String ocrPath, String lang) {
		var req = Map.of(
			"doc_type", docType,
			"json_path", jsonPath,
			"ocr_path", ocrPath == null ? "" : ocrPath,
			"lang", lang
		);

		return fastApiWebClient.post()
			.uri("/generate-doc")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_OCTET_STREAM)
			.bodyValue(req)
			.retrieve()
			.bodyToMono(byte[].class)
			.block();
	}


	private String buildDocNameForDb(RawDocument raw) {
		// DB에 들어갈 표시용 이름
		return "translated-" + raw.getDocumentType() + "-" + raw.getId() + ".docx";
	}

	private String mapLanguageForFastApi(Language language) {
		// FastAPI가 "english"같은 소문자 문자열을 기대하면 이런 식으로 매핑
		return switch (language) {
			case ENGLISH -> "english";
			case JAPANESE -> "japanese";
			case CHINESE -> "chinese";
			case VIETNAMESE -> "vietnamese";
			default -> "english"; // 기본값
		};
	}

	private String mapDocTypeForFastApi(DocumentType type) {
		// FastAPI가 "재학증명서" 같은 한글 문자열을 기대한다면 여기서 매핑
		// 서버 스펙에 맞게 반드시 수정
		return switch (type) {
			case ENROLLMENT_CERTIFICATE -> "재학증명서";
			case FAMILY_RELATIONSHIP_CERTIFICATE -> "가족관계증명서";
			case REAL_ESTATE_REGISTRY -> "부동산등기부등본";
			default -> type.name(); // 서버가 영문 enum도 허용하면 그대로
		};
	}


	public List<TranslatedDocumentResponseDTO> getTranslatedDocumentsByMemberId(Long memberId) {

		List<TranslatedDocument> documents = translatedDocumentRepository.findAllByRawDocument_Member_Id(memberId);

		return documents.stream()
			.map(translated -> {
				var raw = translated.getRawDocument();
				String s3Key = translated.getTranslatedFilePath();
				String presignedDownloadUrl = s3Service.getPresignedUrlToDownload(s3Key).getPath();
				return TranslatedDocumentResponseDTO.builder()
					.rawDocumentId(raw.getId())
					.documentType(raw.getDocumentType())
					.country(raw.getCountry())
					.translatedDocumentId(translated.getId())
					.translatedDocumentName(translated.getTranslatedDocumentName())
					.translatedFilePath(translated.getTranslatedFilePath())
					.presignedDownloadUrl(presignedDownloadUrl)
					.build();
			})
			.toList();
	}
}
