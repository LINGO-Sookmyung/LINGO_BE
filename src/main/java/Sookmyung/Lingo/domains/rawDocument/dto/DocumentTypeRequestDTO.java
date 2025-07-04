package Sookmyung.Lingo.domains.rawDocument.dto;

import Sookmyung.Lingo.domains.enums.DocumentType;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTypeRequestDTO {
	private DocumentType documentType;
}