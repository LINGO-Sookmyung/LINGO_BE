package Sookmyung.Lingo.domain;

import Sookmyung.Lingo.domain.enums.MemberType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    // === 관계 설정 ===

    // 원본 문서
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RawDocument> rawDocuments = new ArrayList<>();

    // === 연관관계 메서드 ===

    // 문서 추가
    public void addRawDocument(RawDocument document) {
        rawDocuments.add(document);
        if (document.getMember() != this) {
            document.setMember(this);
        }
    }

    // 문서 삭제
    public void removeRawDocument(RawDocument document) {
        rawDocuments.remove(document);
        if (document.getMember() == this) {
            document.setMember(null);
        }
    }

    // === 생성 메서드 ===
    public static Member createMember(String email,
                                      String password,
                                      String name,
                                      Date birth,
                                      String phone_num,
                                      MemberType type) {
        Member member = new Member();
        member.email = email;
        member.password = password;
        member.name = name;
        member.birth = birth;
        member.phone_num = phone_num;
        member.memberType = type;

        return member;
    }
}
