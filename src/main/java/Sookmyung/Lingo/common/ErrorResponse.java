package Sookmyung.Lingo.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 에러 응답 객체
 * 클라이언트에 반환되는 에러 정보
 */
@Getter
public class ErrorResponse {
    private final String message;  // 에러 메시지
    private final int status;   // HTTP 상태 코드
    private final String error;    // 에러명 (ErrorCode 이름)

    public ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getHttpStatus().value();
        this.error = errorCode.name();
        this.message = errorCode.getMessage();
    }

    public ErrorResponse(HttpStatus httpStatus, String error, String message) {
        this.status = httpStatus.value();
        this.error = error;
        this.message = message;
    }
}
