package programmers.team6.global.exception.customException;

import programmers.team6.global.exception.code.UnauthorizedErrorCode;

public class UnauthorizedException extends CustomException {

	public UnauthorizedException(UnauthorizedErrorCode errorCode) {
		super(errorCode);
	}
}
