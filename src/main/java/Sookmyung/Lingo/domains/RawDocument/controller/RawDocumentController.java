package Sookmyung.Lingo.domains.RawDocument.controller;

import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import Sookmyung.Lingo.common.dto.DataResponse;
import Sookmyung.Lingo.common.dto.ErrorResponse;
import Sookmyung.Lingo.domains.Member;
import Sookmyung.Lingo.domains.RawDocument.domain.RawDocument;
import Sookmyung.Lingo.domains.RawDocument.dto.RawDocumentRequestDTO;
import Sookmyung.Lingo.domains.RawDocument.service.RawDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api-docs/documents")
@RequiredArgsConstructor
public class RawDocumentController {

	private final RawDocumentService rawDocumentService;

	@Operation(
		summary = "원본 문서 등록 (비회원)",
		description = "문서 파일을 등록, 현재는 로그인 없이만 가능하며, 회원 기능은 추후 연동 예정",
		responses = {
			@ApiResponse(responseCode = "200", description = "등록 성공",
				content = @Content(schema = @Schema(implementation = DataResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "서버 오류",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
		}
	)
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<DataResponse<Long>> uploadRawDocument(
		@Parameter(description = "문서 업로드 요청 DTO", required = true)
		@RequestPart("requestDTO") RawDocumentRequestDTO requestDTO,

		@Parameter(description = "문서 이미지 파일", required = true)
		@RequestPart("file") MultipartFile file
	) {
		RawDocument saved = rawDocumentService.saveRawDocument(requestDTO, null); // 회원 연동 예정

		return ResponseEntity.ok(DataResponse.of(saved.getId(), "문서가 성공적으로 저장되었습니다."));
	}
}