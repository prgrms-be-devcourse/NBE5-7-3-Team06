package programmers.team6.global.exception.code;

import org.springframework.http.HttpStatus;

import programmers.team6.global.exception.ErrorStatus;

public enum ForbiddenErrorCode implements ErrorCode {

	FORBIDDEN_PENDING("회원가입 승인 대기중입니다"),
	FORBIDDEN_NO_AUTHORITY("권한이 불충분합니다.");

	private final String message;
	private final HttpStatus httpStatus = HttpStatus.FORBIDDEN;

	ForbiddenErrorCode(String message) {
		this.message = message;
	}

	@Override
	public ErrorStatus getErrorStatus() {
		return ErrorStatus.FORBIDDEN;
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
