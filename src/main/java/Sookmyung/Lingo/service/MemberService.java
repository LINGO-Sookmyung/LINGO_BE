package Sookmyung.Lingo.service;

import Sookmyung.Lingo.common.CustomException;
import Sookmyung.Lingo.common.ErrorCode;
import Sookmyung.Lingo.domain.Member;
import Sookmyung.Lingo.domain.enums.MemberType;
import Sookmyung.Lingo.dto.findEmail.FindEmailRequest;
import Sookmyung.Lingo.dto.login.LoginRequest;
import Sookmyung.Lingo.dto.login.LoginResponse;
import Sookmyung.Lingo.dto.resetPassword.ResetPasswordRequest;
import Sookmyung.Lingo.dto.resetPassword.ResetPasswordResponse;
import Sookmyung.Lingo.dto.resetPassword.VerifyCodeRequest;
import Sookmyung.Lingo.dto.signup.SignupRequest;
import Sookmyung.Lingo.dto.signup.SignupResponse;
import Sookmyung.Lingo.jwt.JwtToken;
import Sookmyung.Lingo.jwt.JwtTokenProvider;
import Sookmyung.Lingo.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final MailService mailService;

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

        // UserDetails 객체에서 email 추출
        User user = (User) authentication.getPrincipal();
        String email = user.getUsername();

        // Member 객체를 가져오기
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXISTS_MEMBER_EMAIL));

        // refreshToken Redis에 저장
        redisTemplate.opsForValue().set(
                "refresh:" + member.getId(),
                jwtToken.getRefreshToken(),
                jwtTokenProvider.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        // 4. 로그인 성공 시 응답 객체 생성
        LoginResponse response = LoginResponse.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .jwtToken(jwtToken)
                .build();

        return response;
    }

    // 로그아웃
    public void logout(String accessToken) {
        // 1. accessToken으로 사용자 정보 추출
        String email = jwtTokenProvider.getAuthentication(accessToken).getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_MEMBER));

        // 2. Redis에서 해당 사용자의 Refresh Token 삭제
        redisTemplate.delete("refresh:" + member.getId());

        // 3. accessToken 블랙리스트 등록
        long expiration = jwtTokenProvider.getTokenRemainingTime(accessToken);
        redisTemplate.opsForValue().set("blacklist:" + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);

    }

    // 토큰 재발급
    public LoginResponse reissue(String accessToken, String refreshToken) {
        // 1. AccessToken에서 사용자 이메일 추출 (만료돼도 parse 가능)
        String email = jwtTokenProvider.getAuthentication(accessToken).getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_MEMBER));

        // 2. Redis에서 저장된 RefreshToken 가져오기
        String storedRefreshToken = redisTemplate.opsForValue().get("refresh:" + member.getId());

        // 3. 비교
        if (!refreshToken.equals(storedRefreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 4. 새 토큰 발급
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
        JwtToken newToken = jwtTokenProvider.generateToken(authentication);

        // 5. RefreshToken Redis 갱신
        long refreshExpiration = jwtTokenProvider.getTokenRemainingTime(newToken.getRefreshToken());
        redisTemplate.opsForValue().set(
                "refresh:" + member.getId(),
                newToken.getRefreshToken(),
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );

        return LoginResponse.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .jwtToken(newToken)
                .build();
    }

    // 이메일 찾기
    public String findEmail(FindEmailRequest request) {
        // 요청된 이름과 전화번호로 회원 조회
        Member member = memberRepository.findByNameAndPhoneNum(request.getName(), request.getPhoneNum())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        // 회원이 존재하면 이메일 반환
        return member.getEmail();
    }

    // 비밀번호 재설정 요청 시 이메일로 인증 코드 전송
    public void sendVerifyCodeEmail(ResetPasswordRequest request) {
        // 1. 요청된 이메일로 회원 조회
        Member member = memberRepository.findByEmailAndName(request.getEmail(), request.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        // 2. 인증 메일 발송
        try {
            String verificationCode = mailService.sendVerifyCodeMessage(member.getEmail());
            // 3. 인증 코드 Redis에 저장 (key: email, value: code, expiration: 10분)
            redisTemplate.opsForValue().set(
                    "verifyCode:" + member.getEmail(),
                    verificationCode,
                    10, TimeUnit.MINUTES
            );
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_ERROR);
        }
    }

    // 인증 코드 검증 및 비밀번호 재설정
    public ResetPasswordResponse verifyCodeAndResetPassword(VerifyCodeRequest request) {
        // 1. Redis에서 인증 코드 조회
        String storedCode = redisTemplate.opsForValue().get("verifyCode:" + request.getEmail());
        if (storedCode == null || !storedCode.equals(request.getVerificationCode())) {
            throw new CustomException(ErrorCode.NOT_EXISTS_VERIFICATION_CODE);
        }

        // 2. 인증 코드가 유효하면 비밀번호 재설정
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        // 3. 임시비밀번호 발급 & 암호화 후 저장
        String newPassword = createTempPassword();
        member.setTempPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);

        // 4. 응답 객체 생성
        ResetPasswordResponse response = new ResetPasswordResponse();
        response.setNewPassword(newPassword);
        return response;
    }

    private String createTempPassword() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 8; i++) { // 임시 비밀번호 10자리
            int index = random.nextInt(3); // 랜덤으로 0, 1, 2 중 하나 선택

            switch (index) {
                case 0 -> key.append((char) (random.nextInt(26) + 97)); // 소문자
                case 1 -> key.append((char) (random.nextInt(26) + 65)); // 대문자
                case 2 -> key.append(random.nextInt(10)); // 숫자
            }
        }
        return key.toString();
    }
}
