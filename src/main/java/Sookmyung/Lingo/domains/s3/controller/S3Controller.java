package Sookmyung.Lingo.domains.s3.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Sookmyung.Lingo.common.dto.DataResponse;
import Sookmyung.Lingo.common.exception.CustomException;
import Sookmyung.Lingo.common.exception.ErrorCode;
import Sookmyung.Lingo.domains.s3.dto.S3ResponseDTO;
import Sookmyung.Lingo.domains.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api-docs/file")
public class S3Controller {

	private final S3Service s3Service;

	@Operation(summary = "Download용 Presigned URL 생성", description = "다운로드를 위한 Presigned URL을 생성한다")
	@GetMapping("/presigned/download")
	public S3ResponseDTO getPresignedUrlToDownload(@RequestParam(value = "filename") String fileName) throws IOException {
		return s3Service.getPresignedUrlToDownload(fileName);
	}

	@Operation(summary = "Upload용 Presigned URL 생성", description = "업로드를 위한 Presigned URL을 생성한다")
	@PostMapping("/presigned/upload-urls")
	public ResponseEntity<DataResponse<List<S3ResponseDTO>>> getPresignedUrls(
		@RequestBody List<String> originalFileNames
	) {
		List<S3ResponseDTO> urls = originalFileNames.stream()
			.map(s3Service::getPresignedUrlToUpload)
			.toList();

		return ResponseEntity.ok(DataResponse.of(urls, "Presigned URL 생성"));
	}

}