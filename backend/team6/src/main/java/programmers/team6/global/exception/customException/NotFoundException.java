package programmers.team6.global.exception.customException;

import programmers.team6.global.exception.code.NotFoundErrorCode;

public class NotFoundException extends CustomException {

	public NotFoundException(NotFoundErrorCode errorCode) {
		super(errorCode);
	}
}
