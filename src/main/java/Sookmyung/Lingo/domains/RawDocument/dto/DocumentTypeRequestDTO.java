package Sookmyung.Lingo.domains.RawDocument.dto;

import Sookmyung.Lingo.domains.enums.DocumentType;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTypeRequestDTO {
	private DocumentType documentType;
}