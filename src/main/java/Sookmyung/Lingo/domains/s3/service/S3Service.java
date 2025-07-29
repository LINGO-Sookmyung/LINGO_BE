package Sookmyung.Lingo.domains.s3.service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import Sookmyung.Lingo.common.exception.CustomException;
import Sookmyung.Lingo.common.exception.ErrorCode;
import Sookmyung.Lingo.domains.s3.dto.S3ResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	private final AmazonS3 amazonS3;

	private static final long PRESIGNED_URL_EXPIRATION_MINUTES = 3;


	//업로드
	public S3ResponseDTO getPresignedUrlToUpload(String fileName) {
		validateFileName(fileName);

		String uuidFileName = UUID.randomUUID().toString() + getFileExtension(fileName);

		// 폴더 경로 + 파일명
		String s3Key = "origin/" + uuidFileName;

		return S3ResponseDTO.builder()
			.path(generatePresignedUrl(s3Key, HttpMethod.PUT, PRESIGNED_URL_EXPIRATION_MINUTES))
			.s3Key(s3Key) // 실제 저장된 S3 경로
			.build();
	}

	//다운로드
	public S3ResponseDTO getPresignedUrlToDownload(String fileName) {
		validateFileName(fileName);

		return S3ResponseDTO.builder()
			.path(generatePresignedUrl(fileName, HttpMethod.GET, PRESIGNED_URL_EXPIRATION_MINUTES))
			.build();
	}

	//공통
	private String generatePresignedUrl(String fileName, HttpMethod method, long minutes) {
		Date expiration = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes));

		String contentType = MediaTypeFactory
			.getMediaType(fileName)
			.map(MediaType::toString)
			.orElse("application/octet-stream");

		GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, fileName)
			.withMethod(method)
			.withExpiration(expiration);

		if (method == HttpMethod.PUT) {
			request.setContentType(contentType);
		}
		return amazonS3.generatePresignedUrl(request).toString();
	}

	private String getFileExtension(String fileName) {
		int lastDotIndex = fileName.lastIndexOf(".");
		if (lastDotIndex == -1) {
			throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
		}
		return fileName.substring(lastDotIndex);
	}

	//유효성 검사
	private void validateFileName(String fileName) {
		if (fileName == null || fileName.trim().isEmpty()) {
			throw new CustomException(ErrorCode.EMPTY_FILE_NAME);
		}
	}



}
