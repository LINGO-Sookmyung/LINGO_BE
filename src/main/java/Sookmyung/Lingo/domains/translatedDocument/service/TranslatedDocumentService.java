package Sookmyung.Lingo.domains.translatedDocument.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Sookmyung.Lingo.domains.rawDocument.repository.RawDocumentRepository;
import Sookmyung.Lingo.domains.translatedDocument.domain.TranslatedDocument;
import Sookmyung.Lingo.domains.translatedDocument.dto.TranslatedDocumentResponseDTO;
import Sookmyung.Lingo.domains.translatedDocument.repository.TranslatedDocumentRepository;


@Service
public class TranslatedDocumentService {

	private final TranslatedDocumentRepository translatedDocumentRepository;

	@Autowired
	public TranslatedDocumentService(TranslatedDocumentRepository translatedDocumentRepository) {
		this.translatedDocumentRepository = translatedDocumentRepository;
	}

	public List<TranslatedDocumentResponseDTO> getTranslatedDocumentsByMemberId(Long memberId) {
		List<TranslatedDocument> documents = translatedDocumentRepository.findAllByRawDocument_Member_Id(memberId);

		return documents.stream()
			.map(translated -> {
				var raw = translated.getRawDocument();
				return TranslatedDocumentResponseDTO.builder()
					.rawDocumentId(raw.getId())
					.documentType(raw.getDocumentType())
					.country(raw.getCountry())
					.translatedDocumentId(translated.getId())
					.translatedDocumentName(translated.getTranslatedDocumentName())
					.translatedFileUrl(translated.getTranslatedFilePath())
					.build();
			})
			.toList();
	}
}
