package Sookmyung.Lingo.domain;

import jakarta.persistence.*;

@Entity
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
}
