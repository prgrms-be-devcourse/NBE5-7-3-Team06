package programmers.team6.global.exception.code;

import org.springframework.http.HttpStatus;

import programmers.team6.global.exception.ErrorStatus;

public interface ErrorCode {
	ErrorStatus getErrorStatus();

	HttpStatus getHttpStatus();

	int getHttpStatusCode();

	String getMessage();
}
