package Sookmyung.Lingo.domains.member.dto.changePassword;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentPasswordResponse {
    private boolean isValid;
}
