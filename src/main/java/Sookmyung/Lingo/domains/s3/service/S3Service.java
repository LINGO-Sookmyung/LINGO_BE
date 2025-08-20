package Sookmyung.Lingo.domains.s3.service;

import java.io.ByteArrayInputStream;
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
import com.amazonaws.services.s3.model.ObjectMetadata;

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

	private static final long PRESIGNED_URL_EXPIRATION_MINUTES = 30;

	//공통
	private String generatePresignedUrl(String fileName, HttpMethod method, long minutes) {
		Date expiration = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes));

		String contentType = MediaTypeFactory
			.getMediaType(fileName)
			.map(MediaType::toString)
			.orElse("application/octet-stream");

		if (fileName.toLowerCase().endsWith(".docx")) {
			contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
		}

		GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, fileName)
			.withMethod(method)
			.withExpiration(expiration);

		if (method == HttpMethod.PUT) {
			request.setContentType(contentType);
		}
		return amazonS3.generatePresignedUrl(request).toString();
	}


	//업로드
	public S3ResponseDTO getPresignedUrlToUpload(String fileName) {
		validateFileName(fileName);

		String ext = getFileExtension(fileName);
		String s3Key = "origin/" + UUID.randomUUID() + ext;
		String presignedUrl = generatePresignedUrl(s3Key, HttpMethod.PUT, PRESIGNED_URL_EXPIRATION_MINUTES);

		return S3ResponseDTO.builder()
			.path(presignedUrl)
			.s3Key(s3Key)
			.build();
	}


	//다운로드
	public S3ResponseDTO getPresignedUrlToDownload(String fileName) {
		validateFileName(fileName);

		return S3ResponseDTO.builder()
			.path(generatePresignedUrl(fileName, HttpMethod.GET, PRESIGNED_URL_EXPIRATION_MINUTES))
			.build();
	}


	//번역문서 업로드  -> dto
	public String uploadTranslatedDocx(byte[] docxBytes) {
		String s3Key = "translated/" + UUID.randomUUID() + ".docx";

		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		meta.setContentLength(docxBytes.length);

		amazonS3.putObject(bucket, s3Key, new ByteArrayInputStream(docxBytes), meta);

		// 필요하다면 S3 URL 반환 (또는 key만 반환)
		return s3Key;
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
