package Sookmyung.Lingo.dto.resetPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "이름은 필수 항목입니다.")
    private String name;
    @Email(message = "이메일 형식이 잘못되었습니다.")
    @NotBlank(message = "이메일은 필수 항목입니다.")
    private String email;
}
