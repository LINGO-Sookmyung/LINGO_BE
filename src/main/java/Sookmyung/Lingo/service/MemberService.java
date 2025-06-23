package Sookmyung.Lingo.service;

import Sookmyung.Lingo.domain.Member;
import Sookmyung.Lingo.domain.enums.MemberType;
import Sookmyung.Lingo.dto.signup.SignupRequest;
import Sookmyung.Lingo.dto.signup.SignupResponse;
import Sookmyung.Lingo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 - 일반 유저
    public SignupResponse signup(SignupRequest request) {
        if (isEmailDuplicate(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (!request.getPassword().equals(request.getPwConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        Member member = Member.createMember(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                request.getBirth(),
                request.getPhoneNum(),
                MemberType.USER
        );
        memberRepository.save(member);

        SignupResponse response = new SignupResponse();
        response.setMemberId(member.getId());
        response.setEmail(member.getEmail());
        response.setName(member.getName());
        response.setMessage("회원가입이 완료되었습니다.");
        return response;
    }

    // 이메일 중복 체크
    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }
}
