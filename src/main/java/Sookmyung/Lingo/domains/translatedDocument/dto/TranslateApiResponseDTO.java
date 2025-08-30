package Sookmyung.Lingo.domains.translatedDocument.dto;

public record TranslateApiResponseDTO(
	Long rawDocumentId,
	String docType,
	String lang,
	String path,   // FastAPI가 저장한 번역결과 JSON 파일 경로
	Object result  // 번역 JSON 객체
) {}