package Sookmyung.Lingo.jwt;

import lombok.Data;

@Data
public class ReissueRequest {
    private String refreshToken;
}
