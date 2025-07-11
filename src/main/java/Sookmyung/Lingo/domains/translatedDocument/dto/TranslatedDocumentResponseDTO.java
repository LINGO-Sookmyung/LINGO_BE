package Sookmyung.Lingo.domains.translatedDocument.dto;

import Sookmyung.Lingo.domains.enums.Country;
import Sookmyung.Lingo.domains.enums.DocumentType;
import Sookmyung.Lingo.domains.rawDocument.domain.RawDocument;
import Sookmyung.Lingo.domains.translatedDocument.domain.TranslatedDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TranslatedDocumentResponseDTO {

	private Long rawDocumentId;
	private DocumentType documentType;
	private Country country;

	private Long translatedDocumentId;
	private String translatedDocumentName;
	private String translatedFileUrl;

	public static TranslatedDocumentResponseDTO from(RawDocument rawDocument) {
		TranslatedDocument translated = rawDocument.getTranslatedDocument();

		return TranslatedDocumentResponseDTO.builder()
			.rawDocumentId(rawDocument.getId())
			.documentType(rawDocument.getDocumentType())
			.country(rawDocument.getCountry())
			.translatedDocumentId(translated.getId())
			.translatedDocumentName(translated.getTranslatedDocumentName())
			.translatedFileUrl(translated.getTranslatedFilePath())
			.build();
	}
}
