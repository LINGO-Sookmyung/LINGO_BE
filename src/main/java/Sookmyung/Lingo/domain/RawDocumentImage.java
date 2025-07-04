package Sookmyung.Lingo.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    // === 생성자 ===
    @Builder
    public RawDocumentImage(String rawFilename,
                            Long pageNumber,
                            String rawFilePath) { // 필요한 필드만 노출
        this.rawFilename = rawFilename;
        this.pageNumber = pageNumber;
        this.rawFilePath = rawFilePath;
    }
}
