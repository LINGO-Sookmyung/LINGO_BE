package Sookmyung.Lingo.dto.login;

import Sookmyung.Lingo.jwt.JwtToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {
    private Long memberId;
    private String email;
    private JwtToken jwtToken;
}