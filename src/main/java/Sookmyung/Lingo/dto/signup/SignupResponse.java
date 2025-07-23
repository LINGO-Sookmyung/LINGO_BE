package Sookmyung.Lingo.dto.signup;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    private Long memberId;
    private String email;
    private String name;
    private String message;
}
