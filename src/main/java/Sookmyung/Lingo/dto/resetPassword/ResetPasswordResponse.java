package Sookmyung.Lingo.dto.resetPassword;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordResponse {
    private String newPassword;
}
