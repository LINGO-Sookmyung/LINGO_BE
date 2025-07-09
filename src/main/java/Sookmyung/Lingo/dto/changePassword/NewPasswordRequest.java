package Sookmyung.Lingo.dto.changePassword;

import lombok.Data;

@Data
public class NewPasswordRequest {
    private String newPassword;
    private String confirmNewPassword;
}
