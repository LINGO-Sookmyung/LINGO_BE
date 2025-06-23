package Sookmyung.Lingo.dto.signup;

import lombok.Data;

@Data
public class SignupResponse {
    private Long memberId;
    private String email;
    private String name;
    private String message;
}
