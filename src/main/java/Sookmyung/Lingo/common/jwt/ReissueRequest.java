package Sookmyung.Lingo.common.jwt;

import lombok.Data;

@Data
public class ReissueRequest {
    private String refreshToken;
}
