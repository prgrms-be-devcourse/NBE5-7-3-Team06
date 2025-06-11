package programmers.team6.global.exception.code;

import org.springframework.http.HttpStatus;

import programmers.team6.global.exception.ErrorStatus;

public enum BadRequestErrorCode implements ErrorCode {

	BAD_REQUEST_APPROVE("해당 결재를 승인할 수 없습니다"),
	BAD_REQUEST_REJECT("해당 결재를 반려할 수 없습니다"),
	BAD_REQUEST_VALIDATION("필수 입력 항목을 확인해주세요."),
	BAD_REQUEST_INVALID_INPUT("잘못된 입력입니다."),
	BAD_REQUEST_VACATION_OVERLAP("해당 기간에 이미 신청된 휴가가 있습니다."),
	BAD_REQUEST_INSUFFICIENT_VACATION_DAYS("신청한 휴가 일수가 잔여 휴가 일수를 초과했습니다."),
	BAD_REQUEST_DUPLICATE_CODE("중복된 분류 코드입니다."),
	BAD_REQUEST_MEMBER_ROLE("해당 멤버의 권한을 변경할 수 없습니다.");

	private final String message;
	private final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

	BadRequestErrorCode(String message) {
		this.message = message;
	}

	@Override
	public ErrorStatus getErrorStatus() {
		return ErrorStatus.BAD_REQUEST;
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
