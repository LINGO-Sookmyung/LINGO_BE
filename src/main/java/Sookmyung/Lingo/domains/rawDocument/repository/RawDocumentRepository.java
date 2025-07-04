package Sookmyung.Lingo.domains.rawDocument.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Sookmyung.Lingo.domains.rawDocument.domain.RawDocument;

@Repository
public interface RawDocumentRepository extends JpaRepository<RawDocument, Long> {
}
