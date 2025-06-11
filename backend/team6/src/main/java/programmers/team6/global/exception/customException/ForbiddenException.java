package programmers.team6.global.exception.customException;

import programmers.team6.global.exception.code.ForbiddenErrorCode;

public class ForbiddenException extends CustomException {

	public ForbiddenException(ForbiddenErrorCode errorCode) {
		super(errorCode);
	}
}
