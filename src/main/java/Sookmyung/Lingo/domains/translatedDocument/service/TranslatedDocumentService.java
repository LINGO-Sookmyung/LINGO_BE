package Sookmyung.Lingo.domains.translatedDocument.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import Sookmyung.Lingo.domains.s3.service.S3Service;
import Sookmyung.Lingo.domains.translatedDocument.domain.TranslatedDocument;
import Sookmyung.Lingo.domains.translatedDocument.dto.TranslatedDocumentResponseDTO;
import Sookmyung.Lingo.domains.translatedDocument.repository.TranslatedDocumentRepository;


@Service
public class TranslatedDocumentService {

	private final TranslatedDocumentRepository translatedDocumentRepository;
	private final S3Service s3Service;


	@Autowired
	public TranslatedDocumentService(TranslatedDocumentRepository translatedDocumentRepository, S3Service s3Service) {
		this.translatedDocumentRepository = translatedDocumentRepository;
		this.s3Service =s3Service;
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
