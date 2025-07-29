package Sookmyung.Lingo.domains.member.dto.signup;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CheckEmailRequest {
    @Email(message = "이메일 형식이 잘못되었습니다.")
    @NotBlank
    private String email;
}
