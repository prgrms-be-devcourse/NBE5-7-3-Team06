package programmers.team6.global.exception.customException;

import programmers.team6.global.exception.code.ConflictErrorCode;

public class ConflictException extends CustomException {

	public ConflictException(ConflictErrorCode errorCode) {
		super(errorCode);
	}
}
