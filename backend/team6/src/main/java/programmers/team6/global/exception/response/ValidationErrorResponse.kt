package programmers.team6.global.exception.response;

import java.util.Map;

import lombok.Getter;

@Getter
public class ValidationErrorResponse extends ErrorResponse {

	private final Map<String, String> errors;

	public ValidationErrorResponse(String codeName, String message, int status, Map<String, String> errors) {
		super(codeName, message, status);
		this.errors = errors;
	}
}
