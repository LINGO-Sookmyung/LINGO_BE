package Sookmyung.Lingo.dto.changePassword;

import lombok.Data;
import lombok.Getter;

@Getter
public class NewPasswordRequest {
    private String newPassword;
    private String confirmNewPassword;
}
