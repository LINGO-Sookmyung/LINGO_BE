package Sookmyung.Lingo.domains.member.service;

import Sookmyung.Lingo.common.exception.CustomException;
import Sookmyung.Lingo.common.exception.ErrorCode;
import Sookmyung.Lingo.common.jwt.CustomUserDetails;
import Sookmyung.Lingo.common.jwt.JwtToken;
import Sookmyung.Lingo.common.jwt.JwtTokenProvider;
import Sookmyung.Lingo.common.util.AuthUtil;
import Sookmyung.Lingo.domains.member.domain.Member;
import Sookmyung.Lingo.domains.enums.MemberType;
import Sookmyung.Lingo.domains.member.dto.login.LoginRequest;
import Sookmyung.Lingo.domains.member.dto.login.LoginResponse;
import Sookmyung.Lingo.domains.member.dto.resetPassword.ResetPasswordRequest;
import Sookmyung.Lingo.domains.member.dto.resetPassword.ResetPasswordResponse;
import Sookmyung.Lingo.domains.member.dto.resetPassword.VerifyCodeRequest;
import Sookmyung.Lingo.domains.member.dto.signup.CheckEmailResponse;
import Sookmyung.Lingo.domains.member.dto.signup.SignupRequest;
import Sookmyung.Lingo.domains.member.dto.signup.SignupResponse;
import Sookmyung.Lingo.domains.member.repository.MemberRepository;
import Sookmyung.Lingo.domains.member.dto.changePassword.CurrentPasswordRequest;
import Sookmyung.Lingo.domains.member.dto.changePassword.NewPasswordRequest;
import Sookmyung.Lingo.domains.member.dto.findEmail.FindEmailRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final MailService mailService;
    private final AuthUtil authUtil;

    // 회원가입 - 일반 유저
    public SignupResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
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

        return SignupResponse.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .message("회원가입이 완료되었습니다.")
                .build();
    }

    // 이메일 중복 체크
    public CheckEmailResponse isEmailDuplicate(String email) {
        if (memberRepository.existsByEmail(email)) {
            return CheckEmailResponse.builder()
                    .isAvailable(false)
                    .message("이미 사용 중인 이메일입니다.")
                    .build();
        } else {
            return CheckEmailResponse.builder()
                    .isAvailable(true)
                    .message("사용 가능한 이메일입니다.")
                    .build();
        }
    }

    // 로그인
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        // 1. 로그인 정보(이메일, 비밀번호)를 기반으로 Authentication 객체 생성
        // 이때 authentication은 인증 여부를 확인하는 authenticated 값 false
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authentication;
        try {
            // 2. 실제 검증
            // authenticate() 메서드를 통해 요청된 Member에 대한 검증 진행
            // suthenticate 메서드가 실행될 때 CustomUserDetailsService에서 만든 loadUserByUsername 메서드 실행
            authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new CustomException(ErrorCode.INVALID_MEMBER_LOGIN);
        }

        // 3. 인증이 성공하면 JWT 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        // UserDetails 객체에서 email 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

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
        return LoginResponse.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .jwtToken(jwtToken)
                .build();
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
    public LoginResponse reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(ErrorCode.EMPTY_REFRESH_TOKEN);
        }
        // 0. RefreshToken 검증
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_TOKEN);
        }
        // 1. RefreshToken에서 사용자 이메일 추출
        String email = jwtTokenProvider.getUserEmailFromRefresh(refreshToken);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_MEMBER));

        // 2. Redis에서 저장된 RefreshToken 가져오기
        String storedRefreshToken = redisTemplate.opsForValue().get("refresh:" + member.getId());

        // 3. 비교
        if (!refreshToken.equals(storedRefreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 4. 새 토큰 발급 (Authentication 직접 생성)
        List<GrantedAuthority> authorities = member.getRoles().stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                member.getEmail(),
                null,
                authorities
        );

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
        memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
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
        return ResetPasswordResponse.builder()
                .newPassword(newPassword)
                .build();
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

    // 비밀번호 변경 - 현재 비밀번호 확인
    public boolean checkCurrentPassword(Member member, CurrentPasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            return false;
        }
        return true; // 현재 비밀번호가 일치하면 true 반환
    }

    // 비밀번호 변경 - 새 비밀번호로 변경
    public void changePassword(Member member, NewPasswordRequest request) {
        // 새 비밀번호와 확인 비밀번호가 일치하는지 확인
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new CustomException(ErrorCode.NOT_MATCH_PASSWORD_CONFIRM);
        }

        // 새 비밀번호 암호화
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());

        // 비밀번호 변경
        member.updatePassword(encodedNewPassword);
        memberRepository.save(member);
    }
}
