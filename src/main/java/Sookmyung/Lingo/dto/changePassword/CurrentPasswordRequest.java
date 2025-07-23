package Sookmyung.Lingo.dto.changePassword;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;

@Getter
public class CurrentPasswordRequest {
    @NotBlank(message = "현재 비밀번호는 필수 항목입니다.")
    private String currentPassword;
}
