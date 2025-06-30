package Sookmyung.Lingo.controller;

import Sookmyung.Lingo.dto.findEmail.FindEmailRequest;
import Sookmyung.Lingo.dto.login.LoginRequest;
import Sookmyung.Lingo.dto.login.LoginResponse;
import Sookmyung.Lingo.dto.signup.SignupRequest;
import Sookmyung.Lingo.dto.signup.SignupResponse;
import Sookmyung.Lingo.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

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


    // 아이디(이메일) 찾기
    @PostMapping("/find-email")
    public String findEmail(@Valid @RequestBody FindEmailRequest request) {
        return memberService.findEmail(request);
    }

    // 비밀번호 찾기
}
