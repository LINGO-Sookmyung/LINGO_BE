package Sookmyung.Lingo.domains.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Sookmyung.Lingo.domains.member.domain.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Member> findByNameAndPhoneNum(String name, String phoneNum);

    Optional<Member> findByEmailAndName(String email, String name);
}
