package Sookmyung.Lingo.service;

import Sookmyung.Lingo.common.CustomException;
import Sookmyung.Lingo.common.ErrorCode;
import Sookmyung.Lingo.domain.Member;
import Sookmyung.Lingo.domain.enums.MemberType;
import Sookmyung.Lingo.dto.findEmail.FindEmailRequest;
import Sookmyung.Lingo.dto.login.LoginRequest;
import Sookmyung.Lingo.dto.login.LoginResponse;
import Sookmyung.Lingo.dto.signup.SignupRequest;
import Sookmyung.Lingo.dto.signup.SignupResponse;
import Sookmyung.Lingo.jwt.JwtToken;
import Sookmyung.Lingo.jwt.JwtTokenProvider;
import Sookmyung.Lingo.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입 - 일반 유저
    public SignupResponse signup(SignupRequest request) {
        if (isEmailDuplicate(request.getEmail())) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_MEMBER_EMAIL);
        }
        if (!request.getPassword().equals(request.getPwConfirm())) {
            throw new CustomException(ErrorCode.NOT_MATCH_PASSWORD_CONFIRM);
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

    // 로그인
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        // 1. 로그인 정보(이메일, 비밀번호)를 기반으로 Authentication 객체 생성
        // 이때 authentication은 인증 여부를 확인하는 authenticated 값 false
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        // 2. 실제 검증
        // authenticate() 메서드를 통해 요청된 Member에 대한 검증 진행
        // suthenticate 메서드가 실행될 때 CustomUserDetailsService에서 만든 loadUserByUsername 메서드 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증이 성공하면 JWT 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        // 4. 로그인 성공 시 응답 객체 생성
        User user = (User) authentication.getPrincipal();
        String email = user.getUsername();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTS_MEMBER_EMAIL));
        LoginResponse response = LoginResponse.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .jwtToken(jwtToken)
                .build();

        return response;
    }

    // 이메일 찾기
    public String findEmail(FindEmailRequest request) {
        // 요청된 이름과 전화번호로 회원 조회
        Member member = memberRepository.findByNameAndPhoneNum(request.getName(), request.getPhoneNum())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // 회원이 존재하면 이메일 반환
        return member.getEmail();
    }
}
