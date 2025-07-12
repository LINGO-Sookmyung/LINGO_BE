package Sookmyung.Lingo.domains.rawDocument.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawDocumentUploadRequestDTO {

	@Schema(description = "문서 등록 정보")
	private RawDocumentRequestDTO requestDTO;

	@Schema(description = "S3에 사전 업로드된 이미지 파일 경로 목록", example = "[\"1.jpg\", \"2.jpg\"]")
	private List<String> fileNames;
}
