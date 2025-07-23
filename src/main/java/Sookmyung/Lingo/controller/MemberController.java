package Sookmyung.Lingo.controller;

import Sookmyung.Lingo.dto.changePassword.CurrentPasswordRequest;
import Sookmyung.Lingo.dto.changePassword.CurrentPasswordResponse;
import Sookmyung.Lingo.dto.changePassword.NewPasswordRequest;
import Sookmyung.Lingo.dto.findEmail.FindEmailRequest;
import Sookmyung.Lingo.dto.findEmail.FindEmailResponse;
import Sookmyung.Lingo.dto.login.LoginRequest;
import Sookmyung.Lingo.dto.login.LoginResponse;
import Sookmyung.Lingo.dto.resetPassword.ResetPasswordRequest;
import Sookmyung.Lingo.dto.resetPassword.ResetPasswordResponse;
import Sookmyung.Lingo.dto.resetPassword.VerifyCodeRequest;
import Sookmyung.Lingo.dto.signup.CheckEmailRequest;
import Sookmyung.Lingo.dto.signup.CheckEmailResponse;
import Sookmyung.Lingo.dto.signup.SignupRequest;
import Sookmyung.Lingo.dto.signup.SignupResponse;
import Sookmyung.Lingo.jwt.JwtTokenProvider;
import Sookmyung.Lingo.jwt.ReissueRequest;
import Sookmyung.Lingo.service.MailService;
import Sookmyung.Lingo.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED) // 201 응답 반환
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        return memberService.signup(request);
    }

    // 중복 이메일 확인
    @PostMapping("/check-email")
    public CheckEmailResponse checkEmailDuplicate(@Valid @RequestBody CheckEmailRequest request) {
        return memberService.isEmailDuplicate(request.getEmail());
    }

    // 로그인
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return memberService.login(request);
    }


    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        memberService.logout(token);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    // 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<LoginResponse> reissue(HttpServletRequest request,
                                                 @RequestBody ReissueRequest body) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        return ResponseEntity.ok(memberService.reissue(accessToken, body.getRefreshToken()));
    }

    // 아이디(이메일) 찾기
    @PostMapping("/find-email")
    public FindEmailResponse findEmail(@Valid @RequestBody FindEmailRequest request) {
        String email = memberService.findEmail(request);
        return new FindEmailResponse(email);
    }

    // 비밀번호 찾기
    // 1. 비밀번호 찾기 요청
    @PostMapping("/reset-password/send")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // 이메일로 인증 코드 전송
        memberService.sendVerifyCodeEmail(request);
        return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
    }

    // 2. 인증코드 검증 및 재발급
    @PostMapping("/reset-password/verify")
    public ResetPasswordResponse verifyCodeAndResetPassword(@Valid @RequestBody VerifyCodeRequest request) {
        return memberService.verifyCodeAndResetPassword(request);
    }

    // 비밀번호 변경 - 현재 비밀번호 확인
    @PostMapping("/change-password/check-current")
    public CurrentPasswordResponse checkCurrentPassword(@Valid @RequestBody CurrentPasswordRequest request) {
        boolean isValid = memberService.checkCurrentPassword(request);
        return new CurrentPasswordResponse(isValid);
    }

    // 비밀번호 변경 - 새 비밀번호로 변경
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody NewPasswordRequest request) {
        memberService.changePassword(request);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }
}
