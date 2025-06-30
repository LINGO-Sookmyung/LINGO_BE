package Sookmyung.Lingo.domains.RawDocument.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Sookmyung.Lingo.domains.Member;
import Sookmyung.Lingo.domains.RawDocument.domain.RawDocument;
import Sookmyung.Lingo.domains.RawDocument.dto.RawDocumentRequestDTO;
import Sookmyung.Lingo.domains.RawDocument.repository.RawDocumentRepository;
import jakarta.transaction.Transactional;

@Service
public class RawDocumentService {

	private final RawDocumentRepository rawDocumentRepository;

	@Autowired
	public RawDocumentService(RawDocumentRepository rawDocumentRepository) {
		this.rawDocumentRepository = rawDocumentRepository;
	}

	@Transactional
	public RawDocument saveRawDocument(RawDocumentRequestDTO dto, Member member) {
		RawDocument rawDocument = dto.toEntity(member);
		return rawDocumentRepository.save(rawDocument);
	}
}