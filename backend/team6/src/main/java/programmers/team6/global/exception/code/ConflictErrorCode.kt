package programmers.team6.global.exception.code;

import org.springframework.http.HttpStatus;

import programmers.team6.global.exception.ErrorStatus;

public enum ConflictErrorCode implements ErrorCode {

	CONFLICT_EMAIL("중복된 이메일입니다."),
	CONFLICT_APPROVAL_STEP("결재 단계 동기화 실패"),
	CONFLICT_VERSION("버전이 맞지 않습니다");

	private final String message;
	private final HttpStatus httpStatus = HttpStatus.CONFLICT;

	ConflictErrorCode(String message) {
		this.message = message;
	}

	@Override
	public ErrorStatus getErrorStatus() {
		return ErrorStatus.CONFLICT;
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
