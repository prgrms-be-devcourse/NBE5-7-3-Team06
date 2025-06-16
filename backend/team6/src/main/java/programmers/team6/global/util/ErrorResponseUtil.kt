package programmers.team6.global.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import programmers.team6.global.exception.code.ErrorCode;
import programmers.team6.global.exception.response.ErrorResponse;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponseUtil {

	public static void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) {
		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		ErrorResponse errorResponse = new ErrorResponse(errorCode.toString(), errorCode.getMessage(),
			errorCode.getHttpStatusCode());

		try {
			log.warn(errorCode.getMessage());

			ObjectMapper objectMapper = new ObjectMapper();

			objectMapper.writeValue(response.getWriter(), errorResponse);
		} catch (IOException e) {
			log.error("Failed to write error response", e);
		}
	}

}
