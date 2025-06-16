package Sookmyung.Lingo.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class RawDocumentImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "raw_document_image_id")
    private Long id; // pk

    // 원본 이미지 파일 이름
    @Column(nullable = false)
    private String rawFilename;

    // 페이지 순서
    @Column(nullable = false)
    private Long pageNumber;

    // 저장 경로
    @Column(nullable = false)
    private String rawFilePath;

    // === FK ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_document_id")
    private RawDocument rawDocument;


    // === 관계 설정 ===

    // OCR 결과
    @OneToOne(mappedBy = "rawDocumentImage",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private OcrResult ocrResult;

    // === 연관관계 메서드 ===

    // 원본 문서
    public void setRawDocument(RawDocument rawDocument) {
        if (this.rawDocument != null) {
            this.rawDocument.getRawDocumentImages().remove(this);
        }
        this.rawDocument = rawDocument;
        if (rawDocument != null && !rawDocument.getRawDocumentImages().contains(this)) {
            rawDocument.getRawDocumentImages().add(this);
        }
    }

    // ocr 결과
    public void setOcrResult(OcrResult ocrResult) {
        this.ocrResult = ocrResult;
        if (ocrResult != null && ocrResult.getRawDocumentImage() != this) {
            ocrResult.setRawDocumentImage(this);
        }
    }
}
