package Sookmyung.Lingo.domain;

import jakarta.persistence.*;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id; // pk

    // 이메일
    @Column(nullable = false)
    private String email;

    // 비밀번호
    @Column(nullable = false)
    private String password;

    // 회원 유형
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberType memberType;

    // === 관계 설정 ===
    @OneToOne(mappedBy = "member",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private GeneralMember generalMember;
}
