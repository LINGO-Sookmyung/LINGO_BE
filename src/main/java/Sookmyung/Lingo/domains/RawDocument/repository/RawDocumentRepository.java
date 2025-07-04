package Sookmyung.Lingo.domains.RawDocument.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Sookmyung.Lingo.domains.RawDocument.domain.RawDocument;

@Repository
public interface RawDocumentRepository extends JpaRepository<RawDocument, Long> {
}
