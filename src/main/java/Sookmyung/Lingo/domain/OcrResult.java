package Sookmyung.Lingo.domain;

import jakarta.persistence.*;

import java.util.Map;

@Entity
public class OcrResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ocr_result_id")
    private Long id;

    // OCR 결과 원본 파일 경로
    private String rawOcrPath;

    // OCR 결과 정리본 (텍스트 추출 후 구조화, JSON 형태)
    @Column(columnDefinition = "TEXT")
    private String structuredOcr;

    // === FK ===

    // 문서 이미지
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_document_image_id")
    private RawDocumentImage rawDocumentImage;
}
