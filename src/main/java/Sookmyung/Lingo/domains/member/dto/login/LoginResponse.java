package Sookmyung.Lingo.domains.member.dto.login;

import Sookmyung.Lingo.common.jwt.JwtToken;
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