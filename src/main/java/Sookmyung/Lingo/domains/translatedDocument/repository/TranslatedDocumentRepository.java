package Sookmyung.Lingo.domains.translatedDocument.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Sookmyung.Lingo.domains.translatedDocument.domain.TranslatedDocument;

public interface TranslatedDocumentRepository extends JpaRepository<TranslatedDocument, Long> {

	List<TranslatedDocument> findAllByRawDocument_Member_Id(Long memberId);
}
