package Sookmyung.Lingo.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    // === 연관관계 메서드 ===

    // 원본 문서
    public void setRawDocument(RawDocument rawDocument) {
        if (this.rawDocument != null) {
            this.rawDocument.setTranslatedDocument(null);
        }
        this.rawDocument = rawDocument;
        if (rawDocument != null && rawDocument.getTranslatedDocument() != this) {
            rawDocument.setTranslatedDocument(this);
        }
    }

    // === 생성자 ===
    @Builder
    public TranslatedDocument(String translatedDocumentName, String translatedFilePath) {
        this.translatedDocumentName = translatedDocumentName;
        this.translatedFilePath = translatedFilePath;
    }
}
