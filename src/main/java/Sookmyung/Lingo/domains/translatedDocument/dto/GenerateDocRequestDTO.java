package Sookmyung.Lingo.domains.translatedDocument.dto;

import java.util.Map;

public record GenerateDocRequestDTO(
	Long rawDocumentId,
	Map<String, Object> editedContentJson
) {}