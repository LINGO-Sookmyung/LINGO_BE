package Sookmyung.Lingo.domains.member.dto.findEmail;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;

@Getter
public class FindEmailRequest {
    @NotBlank(message = "이름은 필수 항목입니다.")
    String name;
    @NotBlank(message = "전화번호는 필수 항목입니다.")
    @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "전화번호 형식이 잘못되었습니다.")
    String phoneNum;
}
