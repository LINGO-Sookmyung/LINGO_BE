package Sookmyung.Lingo.common.dto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Sookmyung.Lingo.common.ErrorCode;
import Sookmyung.Lingo.common.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

//swagger 테스트
@RestController
@RequestMapping("/api/test")
public class ResponseTestController {

	@Operation(summary = "성공 응답 테스트")
	@ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리됨")
	@GetMapping("/success")
	public ResponseEntity<?> getSuccess() {
		return ResponseEntity.ok(DataResponse.of("성공한 응답입니다!"));
	}

	@Operation(summary = "에러 응답 테스트")
	@ApiResponses({
		@ApiResponse(responseCode = "404", description = "사용자 없음"),
		@ApiResponse(responseCode = "500", description = "기타 에러")
	})
	@GetMapping("/fail")
	public ResponseEntity<?> getFail() {
		throw new CustomException(ErrorCode.NOT_EXISTS_MEMBER_ID);
	}
}