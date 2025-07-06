package Sookmyung.Lingo.domains.rawDocument.dto;

import Sookmyung.Lingo.domains.member.domain.Member;
import Sookmyung.Lingo.domains.rawDocument.domain.RawDocument;
import Sookmyung.Lingo.domains.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "원본 문서 등록 요청 DTO")
public class RawDocumentRequestDTO {

	@Schema(description = "문서 유형", example = "CORPORATE_REGISTER", required = true)
	private DocumentType documentType;

	@Schema(description = "총 페이지 수", example = "1", required = true)
	private Long totalPages;

	@Schema(description = "문서 제출 국가", example = "USA", required = true)
	private Country country;

	@Schema(description = "번역 언어", example = "ENGLISH", required = true)
	private Language language;

	@Schema(description = "문서 발급 경로", example = "INTERNET", required = true)
	private IssuanceChannel issuanceChannel;

	@Schema(description = "문서 방향", example = "PORTRAIT", required = true)
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