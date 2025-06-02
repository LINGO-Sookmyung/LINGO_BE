package Sookmyung.Lingo.domain;

import jakarta.persistence.*;

@Entity
public class TranslatedDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "translated_document_id")
    private Long id;

    @Column(nullable = false)
    private String translatedDocumentName;

    @Column(nullable = false)
    private String translatedFilePath;

    // === FK ===

    // 원본 문서
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_document_id")
    private RawDocument rawDocument;
}
