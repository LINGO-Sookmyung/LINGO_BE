package Sookmyung.Lingo.dto.findEmail;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindEmailResponse {
    private String email; // 찾은 이메일 주소
}
