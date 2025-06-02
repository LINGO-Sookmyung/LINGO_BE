package Sookmyung.Lingo.domain;

import Sookmyung.Lingo.domain.enums.*;
import jakarta.persistence.*;

@Entity
public class RawDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "raw_document_id")
    private Long id; // pk

    // 문서 유형
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    // 총 페이지 수
    @Column(nullable = false)
    private Long totalPages;

    // 제출 국가
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Country country;

    // 번역 언어
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    // 발급 경로
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuanceChannel issuanceChannel;

    // 문서 방향
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Orientation orientation;

    // === FK ===

    // 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
