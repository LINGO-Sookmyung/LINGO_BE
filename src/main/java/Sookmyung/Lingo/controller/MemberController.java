package Sookmyung.Lingo.controller;

import Sookmyung.Lingo.dto.findEmail.FindEmailRequest;
import Sookmyung.Lingo.dto.findEmail.FindEmailResponse;
import Sookmyung.Lingo.dto.login.LoginRequest;
import Sookmyung.Lingo.dto.login.LoginResponse;
import Sookmyung.Lingo.dto.signup.SignupRequest;
import Sookmyung.Lingo.dto.signup.SignupResponse;
import Sookmyung.Lingo.jwt.JwtTokenProvider;
import Sookmyung.Lingo.jwt.ReissueRequest;
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
    @GetMapping("/check-email")
    public boolean checkEmailDuplicate(@RequestParam String email) {
        return memberService.isEmailDuplicate(email);
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
    public ResponseEntity<LoginResponse> reissue(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody ReissueRequest request) {

        String accessToken = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : bearerToken;
        LoginResponse response = memberService.reissue(accessToken, request.getRefreshToken());
        return ResponseEntity.ok(response);
    }


    // 아이디(이메일) 찾기
    @PostMapping("/find-email")
    public FindEmailResponse findEmail(@Valid @RequestBody FindEmailRequest request) {
        String email = memberService.findEmail(request);
        return new FindEmailResponse(email);
    }

    // 비밀번호 찾기
}
