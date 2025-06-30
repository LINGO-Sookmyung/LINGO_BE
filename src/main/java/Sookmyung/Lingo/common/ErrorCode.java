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
    NOT_EXISTS_MEMBER_EMAIL(HttpStatus.NOT_FOUND, "M-001", "입력하신 이메일로 등록된 계정이 없습니다."),
    ALREADY_EXISTS_MEMBER_EMAIL(HttpStatus.CONFLICT, "M-002", "이미 등록된 이메일입니다."),
    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "M-003", "일치하는 회원 정보를 찾을 수 없습니다."),
    NOT_MATCH_PASSWORD_CONFIRM(HttpStatus.BAD_REQUEST, "M-004", "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    UNAUTHORIZED_MEMBER(HttpStatus.UNAUTHORIZED, "M-005", "회원 인증 정보가 유효하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String errorCode; // 커스텀 에러 코드 -> http status code만으로는 정학한 원인 파악이 어렵기 때문
    private final String message;
}
