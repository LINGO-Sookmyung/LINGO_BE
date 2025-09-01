package Sookmyung.Lingo.domains.member.dto.reissue;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ReissueRequest {
    @NotBlank(message = "리프레시 토큰은 필수 항목입니다.")
    private String refreshToken;
}
