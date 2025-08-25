package Sookmyung.Lingo.domains.rawDocument.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Sookmyung.Lingo.common.dto.DataResponse;
import Sookmyung.Lingo.common.dto.ErrorResponse;
import Sookmyung.Lingo.common.exception.CustomException;
import Sookmyung.Lingo.common.util.AuthUtil;
import Sookmyung.Lingo.domains.member.domain.Member;
import Sookmyung.Lingo.domains.rawDocument.domain.RawDocument;
import Sookmyung.Lingo.domains.rawDocument.dto.RawDocumentUploadRequestDTO;
import Sookmyung.Lingo.domains.rawDocument.service.RawDocumentService;
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
		summary = "재학증명서 원본 문서 등록 - Presigned URL 방식",
		description = "문서 파일은 사전에 Presigned URL로 업로드하고, S3 경로와 함께 문서 DTO를 전송",
		responses = {
			@ApiResponse(responseCode = "200", description = "등록 성공",
				content = @Content(schema = @Schema(implementation = DataResponse.class))),
			@ApiResponse(responseCode = "400", description = "요청 에러",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
		}
	)
	@PostMapping(value = "/enrollment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TranslatedDocumentResultDTO> registerEnrollment(
		@RequestBody RawDocumentUploadRequestDTO uploadRequest
	) {
		Member member = getCurrentMemberOrNull();

		RawDocument saved = rawDocumentService.saveRawDocument(
			uploadRequest.getRequestDTO(),
			uploadRequest.getFileNames(),
			member
		);
		TranslatedDocumentResultDTO translatedDocumentResultDTO = translatedDocumentService.translateAndSave(saved);
		return ResponseEntity.ok(translatedDocumentResultDTO);
	}

	@Operation(
		summary = "가족관계증명서 원본 문서 등록 - Presigned URL 방식",
		description = "문서 파일은 사전에 Presigned URL로 업로드하고, S3 경로와 함께 문서 DTO를 전송",
		responses = {
			@ApiResponse(responseCode = "200", description = "등록 성공",
				content = @Content(schema = @Schema(implementation = DataResponse.class))),
			@ApiResponse(responseCode = "400", description = "요청 에러",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
		}
	)
	@PostMapping(value = "/family-relationship", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TranslatedDocumentResultDTO> registerFamilyRelationship(
		@RequestBody RawDocumentUploadRequestDTO uploadRequest
	) {
		Member member = getCurrentMemberOrNull();

		RawDocument saved = rawDocumentService.saveRawDocument(
			uploadRequest.getRequestDTO(),
			uploadRequest.getFileNames(),
			member
		);
		TranslatedDocumentResultDTO translatedDocumentResultDTO = translatedDocumentService.translateAndSave(saved);
		return ResponseEntity.ok(translatedDocumentResultDTO);
	}
}