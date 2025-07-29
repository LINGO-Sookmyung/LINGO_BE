package Sookmyung.Lingo.domains.member.dto.login;

import Sookmyung.Lingo.common.jwt.JwtToken;
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