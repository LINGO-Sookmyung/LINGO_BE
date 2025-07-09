package Sookmyung.Lingo.dto.changePassword;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurrentPasswordResponse {
    private boolean isValid;
}
