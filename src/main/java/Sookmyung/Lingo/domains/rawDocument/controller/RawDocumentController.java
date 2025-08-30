package Sookmyung.Lingo.domains.rawDocument.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Sookmyung.Lingo.common.dto.DataResponse;
import Sookmyung.Lingo.common.dto.ErrorResponse;
import Sookmyung.Lingo.common.exception.CustomException;
import Sookmyung.Lingo.common.exception.ErrorCode;
import Sookmyung.Lingo.common.util.AuthUtil;
import Sookmyung.Lingo.domains.member.domain.Member;
import Sookmyung.Lingo.domains.rawDocument.domain.RawDocument;
import Sookmyung.Lingo.domains.rawDocument.dto.RawDocumentUploadRequestDTO;
import Sookmyung.Lingo.domains.rawDocument.repository.RawDocumentRepository;
import Sookmyung.Lingo.domains.rawDocument.service.RawDocumentService;
import Sookmyung.Lingo.domains.translatedDocument.dto.GenerateDocRequestDTO;
import Sookmyung.Lingo.domains.translatedDocument.dto.TranslateApiResponseDTO;
import Sookmyung.Lingo.domains.translatedDocument.dto.TranslateResultDTO;
import Sookmyung.Lingo.domains.translatedDocument.dto.TranslatedDocumentResultDTO;
import Sookmyung.Lingo.domains.translatedDocument.service.TranslatedDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/register-raw-documents") // <-- "/api-docs" 대신 안전한 prefix
@RequiredArgsConstructor
@Slf4j
public class RawDocumentController {

	private final RawDocumentService rawDocumentService;
	private final TranslatedDocumentService translatedDocumentService;
	private final AuthUtil authUtil;

	private Member getCurrentMemberOrNull() {
		try {
			return authUtil.getCurrentMember();
		} catch (CustomException e) {
			log.info("비회원 접근으로 간주: {}", e.getErrorCode().getMessage());
			return null;
		}
	}

	@Operation(
		summary = "증명서 번역 미리보기, 증명서 원본 문서 등록하고 번역본 확인후 수정할 수 있도록 문서 생성을 위한 번역 path와 번역 json 파일 반환",
		description = """
        - 원본 문서를 presigned url 방식으로 등록한
        - 다역된 json을 반환하여 사용자가 확인 및 수정할 수 있도록 한다
        - 응답에는 파일 경로(path), 번역된 json 전체(result)가 포함된다
        - `result`는 프론트에서 필요한 key만 보여주고 수정 후 다시 서버에 전달한다
        """,
		responses = {
			@ApiResponse(responseCode = "200", description = "번역 미리보기 성공",
				content = @Content(schema = @Schema(implementation = TranslateApiResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "요청 값이 잘못됨",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "서버 내부 오류",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
		}
	)
	@PostMapping(
		value = "/translate-preview",
		consumes = MediaType.APPLICATION_JSON_VALUE,
		produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<TranslateApiResponseDTO> previewEnrollment(
		@RequestBody RawDocumentUploadRequestDTO uploadRequest
	) {
		Member member = getCurrentMemberOrNull();
		RawDocument saved = rawDocumentService.saveRawDocument(
			uploadRequest.getRequestDTO(), uploadRequest.getFileNames(), member
		);
		TranslateApiResponseDTO preview = translatedDocumentService.preparePreview(saved);
		return ResponseEntity.ok(preview);
	}


	@Operation(
		summary = "최종 번역 문서 확정 및 저장",
		description = """
        번역 미리보기 단계에서 받은 json을 사용자가 수정한 후,
        수정된 json을(editedContentJson)을 서버에 전송하면
        최종 번역 문서를 생성하여 S3에 업로드하고 presigned url을 반환함.
        """,
		responses = {
			@ApiResponse(responseCode = "200", description = "문서 생성 성공",
				content = @Content(schema = @Schema(implementation = TranslatedDocumentResultDTO.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "RawDocument를 찾을 수 없음",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "문서 생성 실패",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
		}
	)
	@PostMapping(value = "/generate-doc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TranslatedDocumentResultDTO> finalizeDocument(@RequestBody GenerateDocRequestDTO body) {
		RawDocument raw = rawDocumentService.getOrThrow(body.rawDocumentId());

		String contentJson;
		try {
			contentJson = new com.fasterxml.jackson.databind.ObjectMapper()
				.writeValueAsString(body.editedContentJson());
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			throw new IllegalArgumentException("editedContentJson 직렬화 실패", e);
		}

		TranslatedDocumentResultDTO result =
			translatedDocumentService.finalizeAndSave(raw, contentJson);
		return ResponseEntity.ok(result);
	}

}