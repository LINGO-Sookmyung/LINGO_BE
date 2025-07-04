package Sookmyung.Lingo.domains.RawDocument.dto;

import Sookmyung.Lingo.domains.Member;
import Sookmyung.Lingo.domains.RawDocument.domain.RawDocument;
import Sookmyung.Lingo.domains.enums.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawDocumentRequestDTO {

	private DocumentType documentType;
	private Long totalPages;
	private Country country;
	private Language language;
	private IssuanceChannel issuanceChannel;
	private Orientation orientation;

	public static RawDocumentRequestDTO from(
		DocumentTypeRequestDTO documentTypeRequestDTO,
		TranslationInfoRequestDTO translationInfoRequestDTO
	) {
		return RawDocumentRequestDTO.builder()
			.documentType(documentTypeRequestDTO.getDocumentType())
			.totalPages(translationInfoRequestDTO.getTotalPages())
			.country(translationInfoRequestDTO.getCountry())
			.language(translationInfoRequestDTO.getLanguage())
			.issuanceChannel(translationInfoRequestDTO.getIssuanceChannel())
			.orientation(translationInfoRequestDTO.getOrientation())
			.build();
	}

	public RawDocument toEntity(Member member) {
		return RawDocument.createRawDocument(
			documentType,
			totalPages,
			country,
			language,
			issuanceChannel,
			orientation,
			member
		);
	}
}