package Sookmyung.Lingo.domains.rawDocument.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import Sookmyung.Lingo.common.dto.DataResponse;
import Sookmyung.Lingo.common.dto.ErrorResponse;
import Sookmyung.Lingo.common.exception.CustomException;
import Sookmyung.Lingo.common.util.AuthUtil;
import Sookmyung.Lingo.domains.member.domain.Member;
import Sookmyung.Lingo.domains.rawDocument.domain.RawDocument;
import Sookmyung.Lingo.domains.rawDocument.dto.RawDocumentRequestDTO;
import Sookmyung.Lingo.domains.rawDocument.service.RawDocumentService;
import Sookmyung.Lingo.domains.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api-docs/documents")
@RequiredArgsConstructor
@Slf4j
public class RawDocumentController {

	private final RawDocumentService rawDocumentService;
	private final S3Service s3Service;
	private final AuthUtil authUtil;


	@Operation(
		summary = "원본 문서 등록 (비회원)",
		description = "문서 DTO + 다중 이미지 파일을 업로드하여 S3에 저장하고 DB에 등록합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "등록 성공",
				content = @Content(schema = @Schema(implementation = DataResponse.class))),
			@ApiResponse(responseCode = "400", description = "요청 에러",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
		}
	)
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<DataResponse<Long>> uploadRawDocument(
		@Parameter(description = "문서 DTO", required = true)
		@RequestPart("requestDTO") RawDocumentRequestDTO requestDTO,

		@Parameter(description = "이미지 파일 목록", required = true)
		@RequestPart("files") List<MultipartFile> files
	) {
		List<String> imageUrls = s3Service.uploadFile(files);

		Member member = null;
		try {
			member = authUtil.getCurrentMember();
		} catch (CustomException e) {
			log.info("비회원 접근으로 간주: {}", e.getErrorCode().getMessage());
		}

		RawDocument saved = rawDocumentService.saveRawDocument(requestDTO, imageUrls, member);
		return ResponseEntity.ok(DataResponse.of(saved.getId(), "문서가 성공적으로 저장되었습니다."));
	}
}