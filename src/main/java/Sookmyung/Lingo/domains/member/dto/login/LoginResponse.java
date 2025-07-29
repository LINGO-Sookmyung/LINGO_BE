package Sookmyung.Lingo.dto.login;

import Sookmyung.Lingo.jwt.JwtToken;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long memberId;
    private String email;
    private JwtToken jwtToken;
}