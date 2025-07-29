package Sookmyung.Lingo.domains.member.dto.changePassword;

import lombok.Getter;

@Getter
public class NewPasswordRequest {
    private String newPassword;
    private String confirmNewPassword;
}
