package Sookmyung.Lingo.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 글로벌 예외 처리기
 * 모든 예외를 일관된 포맷으로 반환
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ErrorCode Enum에 정의된 비즈니스 예외 처리
     */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException 발생: {}", e.getErrorCode(), e);
        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(errorResponse);
    }

    /**
     * 기타 모든 예외 처리 - 표준 예외 등
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled Exception 발생: {}", e.getMessage(), e);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
