package Sookmyung.Lingo.domains.member.dto.signup;

import java.time.LocalDate;

import Sookmyung.Lingo.domains.enums.MemberType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SignupRequest {
    @Email(message = "이메일 형식이 잘못되었습니다.")
    @NotBlank
    private String email;
    private String password;
    private String pwConfirm;
    private String name;
    private LocalDate birth;
    @NotBlank
    @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "전화번호 형식이 잘못되었습니다.")
    private String phoneNum;
    private MemberType memberType = MemberType.USER;
}
