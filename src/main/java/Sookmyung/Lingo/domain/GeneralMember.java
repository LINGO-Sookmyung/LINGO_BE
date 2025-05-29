package Sookmyung.Lingo.domain;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class GeneralMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "general_member_id")
    private Long id;

    // 회원 이름
    @Column(nullable = false)
    private String name;

    // 생년월일
    @Column(nullable = false)
    private Date birth;

    // 전화번호
    @Column(nullable = false)
    private String phone_num;

    // === 관계 설정 ===
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}
