package Sookmyung.Lingo.domain;

import jakarta.persistence.*;

import java.util.Date;

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

    // 회원 이름
    @Column(name = "member_name", nullable = false)
    private String name;

    // 생년월일
    @Column(nullable = false)
    private Date birth;

    // 전화번호
    @Column(nullable = false)
    private String phone_num;

    // 회원 유형
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberType memberType;
}
