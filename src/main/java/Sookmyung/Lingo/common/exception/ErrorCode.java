package Sookmyung.Lingo.common.exception;

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
    UNAUTHORIZED_MEMBER(HttpStatus.UNAUTHORIZED, "M-005", "회원 인증 정보가 유효하지 않습니다."),
    INVALID_MEMBER_LOGIN(HttpStatus.UNAUTHORIZED, "M-006", "아이디 또는 비밀번호가 일치하지 않습니다."),

    // T - token 관련 에러 코드
    UNAUTHORIZED_TOKEN(HttpStatus.UNAUTHORIZED, "T-001", "유효하지 않은 토큰입니다."),
    INVALID_TOKEN_AUTHORITY(HttpStatus.FORBIDDEN, "T-002", "토큰 권한이 유효하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "T-003", "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "T-004", "만료된 액세스 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "T-005", "만료된 리프레시 토큰입니다."),
    EMPTY_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "T-006", "리프레시 토큰이 존재하지 않습니다."),

    // E - email 인증 관련 에러 코드
    EMAIL_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E-001", "이메일 발송 중 오류가 발생했습니다."),
    NOT_MATCH_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "E-002", "인증 코드가 일치하지 않습니다."),
    NOT_EXISTS_VERIFICATION_CODE(HttpStatus.NOT_FOUND, "E-003", "인증 코드가 존재하지 않습니다."),
    //F - file 파일 업로드 관련
    NO_FILE_UPLOADED(HttpStatus.BAD_REQUEST, "F-001", "업로드된 이미지가 없습니다."),
    FILE_TOTAL_PAGE_MISMATCH(HttpStatus.BAD_REQUEST, "F-002", "총 페이지 수와 업로드된 이미지 수가 일치하지 않습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "F-003", "파일에 확장자가 없습니다."),
    EMPTY_FILE_NAME(HttpStatus.BAD_REQUEST, "F-004", "파일 이름이 비어 있습니다."),

    // D - document 관련 에러 코드
    NOT_FOUND_RAW_DOCUMENT(HttpStatus.NOT_FOUND, "D-001", "존재하지 않는 원본 문서입니다."),
    NOT_FOUND_TRANSLATED_DOCUMENT(HttpStatus.NOT_FOUND, "D-002", "존재하지 않는 번역 문서입니다."),
    TRANSLATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "D-003", "문서 번역에 실패했습니다."),
    INVALID_DOCUMENT_TYPE(HttpStatus.BAD_REQUEST, "D-004", "잘못된 문서 유형입니다.");

    private final HttpStatus httpStatus;
    private final String errorCode; // 커스텀 에러 코드 -> http status code만으로는 정학한 원인 파악이 어렵기 때문
    private final String message;
}
