package programmers.team6.global.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import programmers.team6.global.exception.code.BadRequestErrorCode
import programmers.team6.global.exception.customException.CustomException
import programmers.team6.global.exception.response.ErrorResponse
import programmers.team6.global.exception.response.ValidationErrorResponse
import programmers.team6.global.util.logger
import java.time.LocalDateTime
import java.util.function.Consumer


@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CustomException::class)
    fun handleNotFoundException(e: CustomException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode

        this.logger().warn { errorCode.message }

        return ResponseEntity.status(errorCode.httpStatus)
            .body(
                ErrorResponse(
                    errorCode.toString(), errorCode.message,
                    errorCode.httpStatusCode
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors: MutableMap<String, String> = HashMap()

        e.bindingResult.fieldErrors.forEach(Consumer { error: FieldError ->
            val fieldName = error.field
            val message = error.defaultMessage ?: "오류 발생"
            errors[fieldName] = message
        })

        for (key in errors.keys) {
            this.logger().warn { errors[key] }
        }

        val badRequestValidation = BadRequestErrorCode.BAD_REQUEST_VALIDATION

        return ResponseEntity.status(badRequestValidation.httpStatusCode)
            .body(
                ValidationErrorResponse(
                    badRequestValidation.name,
                    badRequestValidation.message,
                    badRequestValidation.httpStatusCode,
                    errors
                )
            )
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<Any> {
        val body: MutableMap<String, Any?> = LinkedHashMap()
        body["timestamp"] = LocalDateTime.now()
        body["status"] = HttpStatus.BAD_REQUEST.value()
        body["error"] = "Bad Request"
        body["message"] = ex.message

        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<Any> {
        val body: MutableMap<String, Any?> = LinkedHashMap()
        body["timestamp"] = LocalDateTime.now()
        body["status"] = HttpStatus.CONFLICT.value()
        body["error"] = "Conflict"
        body["message"] = ex.message

        return ResponseEntity(body, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception): ResponseEntity<Any> {
        val body: MutableMap<String, Any> = LinkedHashMap()
        body["timestamp"] = LocalDateTime.now()
        body["status"] = HttpStatus.INTERNAL_SERVER_ERROR.value()
        body["error"] = "Internal Server Error"
        body["message"] = "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."
        // 실제 오류 메시지는 로그에만 남김
        ex.printStackTrace()

        return ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
