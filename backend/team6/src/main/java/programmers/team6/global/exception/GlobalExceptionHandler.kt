package programmers.team6.global.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import programmers.team6.global.exception.code.BadRequestErrorCode;
import programmers.team6.global.exception.code.ErrorCode;
import programmers.team6.global.exception.customException.CustomException;
import programmers.team6.global.exception.response.ErrorResponse;
import programmers.team6.global.exception.response.ValidationErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleNotFoundException(CustomException e) {
		ErrorCode errorCode = e.getErrorCode();

		log.warn(errorCode.getMessage());

		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(new ErrorResponse(errorCode.toString(), errorCode.getMessage(),
				errorCode.getHttpStatusCode()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		Map<String, String> errors = new HashMap<>();

		e.getBindingResult().getFieldErrors().forEach(error -> {
			String fieldName = error.getField();
			String message = error.getDefaultMessage();
			errors.put(fieldName, message);
		});

		for (String key : errors.keySet()) {
			log.warn(errors.get(key));
		}

		BadRequestErrorCode badRequestValidation = BadRequestErrorCode.BAD_REQUEST_VALIDATION;

		return ResponseEntity.status(badRequestValidation.getHttpStatusCode())
			.body(new ValidationErrorResponse(badRequestValidation.toString(), badRequestValidation.getMessage(),
				badRequestValidation.getHttpStatusCode(), errors));
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("error", "Bad Request");
		body.put("message", ex.getMessage());

		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", HttpStatus.CONFLICT.value());
		body.put("error", "Conflict");
		body.put("message", ex.getMessage());

		return new ResponseEntity<>(body, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleAllExceptions(Exception ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		body.put("error", "Internal Server Error");
		body.put("message", "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요.");
		// 실제 오류 메시지는 로그에만 남김
		ex.printStackTrace();

		return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
