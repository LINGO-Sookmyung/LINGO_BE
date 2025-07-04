package Sookmyung.Lingo.common.dto;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataResponse<T> {
	private final boolean isSuccess;
	private final int status;
	private final String message;
	private final T data;

	@Builder
	protected DataResponse(boolean isSuccess, int status, String message, T data) {
		this.isSuccess = isSuccess;
		this.status = status;
		this.message = message;
		this.data = data;
	}

	public static <T> DataResponse<T> of(T data) {
		return DataResponse.<T>builder()
			.isSuccess(true)
			.status(HttpStatus.OK.value())
			.message("요청이 성공적으로 처리되었습니다.")
			.data(data)
			.build();
	}

	public static <T> DataResponse<T> of(T data, String message) {
		return DataResponse.<T>builder()
			.isSuccess(true)
			.status(HttpStatus.OK.value())
			.message(message)
			.data(data)
			.build();
	}
}