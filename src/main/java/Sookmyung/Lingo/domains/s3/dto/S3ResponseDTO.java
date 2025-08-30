package Sookmyung.Lingo.domains.s3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "AWS S3 URL 응답 정보")
@Getter
@Setter
@Builder
public class S3ResponseDTO {

	private String path;
	private String s3Key;
}
