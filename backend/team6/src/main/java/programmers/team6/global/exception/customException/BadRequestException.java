package programmers.team6.global.exception.customException;

import programmers.team6.global.exception.code.BadRequestErrorCode;

public class BadRequestException extends CustomException {

	public BadRequestException(BadRequestErrorCode errorCode) {
		super(errorCode);
	}
}
