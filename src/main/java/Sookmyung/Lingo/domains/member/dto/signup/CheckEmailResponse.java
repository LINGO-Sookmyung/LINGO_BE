package Sookmyung.Lingo.domains.member.dto.signup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckEmailResponse {
    private boolean isAvailable; // 이메일 사용 가능 여부
    private String message; // 이메일 사용 가능 여부에 대한 메시지
}
