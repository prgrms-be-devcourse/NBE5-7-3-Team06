package programmers.team6.global.exception.customException;

import lombok.Getter;
import programmers.team6.global.exception.code.ErrorCode;

@Getter
public abstract class CustomException extends RuntimeException {

	private final ErrorCode errorCode;

	protected CustomException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
