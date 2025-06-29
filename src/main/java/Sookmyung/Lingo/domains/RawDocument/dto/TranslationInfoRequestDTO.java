package Sookmyung.Lingo.domains.RawDocument.dto;

import Sookmyung.Lingo.domains.enums.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranslationInfoRequestDTO {

	private Long totalPages;
	private Country country;
	private Language language;
	private IssuanceChannel issuanceChannel;
	private Orientation orientation;
}