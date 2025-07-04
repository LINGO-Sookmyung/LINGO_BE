package Sookmyung.Lingo.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * (ex) NOT_EXISTS_MEMBER_ID(HttpStatus.NOT_FOUND, "M-001", "존재하지 않는 회원 아이디입니다."),
 * 위 예와 같이 에러코드들 Enum으로 관리
 * 형식: errorName(HttpStatus, "errorCode", "message");
 * 에러코드는 관련 기능 이름 첫번째 알파벳-번호 형식으로 작성
 */

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // M - member 관련 에러 코드
    NOT_EXISTS_MEMBER_ID(HttpStatus.NOT_FOUND, "M-001", "존재하지 않는 회원 아이디입니다.");

    private final HttpStatus httpStatus;
    private final String errorCode; // 커스텀 에러 코드 -> http status code만으로는 정학한 원인 파악이 어렵기 때문
    private final String message;
}
