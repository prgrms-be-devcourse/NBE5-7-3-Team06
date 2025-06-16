package programmers.team6.global.exception.code;

import org.springframework.http.HttpStatus;

import programmers.team6.global.exception.ErrorStatus;

public enum UnauthorizedErrorCode implements ErrorCode {

	UNAUTHORIZED_PASSWORD("비밀번호가 일치하지 않습니다"),
	UNAUTHORIZED_INVALID_TOKEN("유효하지 않은 토큰입니다."),
	UNAUTHORIZED_BLACKLIST_TOKEN("블랙리스트에 등록된 토큰입니다."),
	UNAUTHORIZED_INVALID_SIGNATURE("서명이 유효하지 않습니다."),
	UNAUTHORIZED_MALFORMED_TOKEN("구조가 잘못된 JWT 입니다."),
	UNAUTHORIZED_ILLEGAL_ARGUMENT_TOKEN("비어있거나 잘못된 JWT 입니다."),
	UNAUTHORIZED_UNSUPPORTED_TOKEN("지원하지 않는 토큰 형식입니다."),
	UNAUTHORIZED_INVALID_HEADER("요청 헤더가 유효하지 않습니다."),
	UNAUTHORIZED_EXPIRED_TOKEN("만료된 토큰입니다."),
	UNAUTHORIZED_ENTRY_POINT("인증이 필요한 요청입니다.");

	private final String message;
	private final HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;

	UnauthorizedErrorCode(String message) {
		this.message = message;
	}

	@Override
	public ErrorStatus getErrorStatus() {
		return ErrorStatus.UNAUTHORIZED;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	@Override
	public int getHttpStatusCode() {
		return httpStatus.value();
	}

	@Override
	public String getMessage() {
		return message;
	}
}
