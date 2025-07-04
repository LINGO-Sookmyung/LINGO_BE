package Sookmyung.Lingo.domains.s3.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import Sookmyung.Lingo.common.dto.DataResponse;
import Sookmyung.Lingo.common.dto.ErrorResponse;
import Sookmyung.Lingo.domains.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api-docs/file")
public class S3Controller {

	private final S3Service s3Service;

	@Operation(
		summary = "S3 파일 업로드",
		description = "MultipartFile 형태의 여러 파일을 S3에 업로드합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "업로드 성공",
				content = @Content(schema = @Schema(implementation = DataResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "서버 오류",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
		}
	)
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<DataResponse<List<String>>> uploadFile(
		@Parameter(description = "업로드할 파일들", required = true)
		@RequestPart("files") List<MultipartFile> multipartFiles
	) {
		List<String> urls = s3Service.uploadFile(multipartFiles);
		return ResponseEntity.ok(DataResponse.of(urls, "파일 업로드 성공"));
	}

	@Operation(
		summary = "S3 파일 삭제",
		description = "파일명을 기반으로 S3에서 해당 파일을 삭제합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "삭제 성공",
				content = @Content(schema = @Schema(implementation = DataResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "서버 오류",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
		}
	)
	@DeleteMapping
	public ResponseEntity<DataResponse<String>> deleteFile(
		@Parameter(description = "삭제할 파일 이름", required = true)
		@RequestParam String fileName
	) {
		s3Service.deleteFile(fileName);
		return ResponseEntity.ok(DataResponse.of(fileName, "파일 삭제 성공"));
	}
}