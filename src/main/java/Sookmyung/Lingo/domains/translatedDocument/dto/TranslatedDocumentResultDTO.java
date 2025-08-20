package Sookmyung.Lingo.domains.translatedDocument.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranslatedDocumentResultDTO {
	private Long rawDocumentId;
	private Long translatedDocumentId;
	private String translatedName;
	private String resultS3Key;
	private String presignedDownloadUrl;
	private String contentType;
}