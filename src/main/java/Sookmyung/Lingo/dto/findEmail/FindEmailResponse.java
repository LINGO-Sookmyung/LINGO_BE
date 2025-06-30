package Sookmyung.Lingo.dto.findEmail;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FindEmailResponse {
    private String email; // 찾은 이메일 주소
}
