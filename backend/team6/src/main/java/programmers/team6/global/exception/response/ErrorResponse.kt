package programmers.team6.global.exception.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

	private final String codeName;
	private final String message;
	private final int status;

}
