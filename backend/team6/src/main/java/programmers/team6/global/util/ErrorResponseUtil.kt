package programmers.team6.global.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletResponse
import lombok.AccessLevel
import lombok.NoArgsConstructor
import lombok.extern.slf4j.Slf4j
import programmers.team6.global.exception.code.ErrorCode
import programmers.team6.global.exception.response.ErrorResponse
import java.io.IOException
import kotlin.math.log


object ErrorResponseUtil {

    private val logger = KotlinLogging.logger {}

    fun setErrorResponse(response: HttpServletResponse, errorCode: ErrorCode) {
        response.status = errorCode.httpStatus.value()
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"

        val errorResponse = ErrorResponse(
            errorCode.toString(), errorCode.message,
            errorCode.httpStatusCode
        )

        try {
            logger.warn { errorCode.message }
            val objectMapper = ObjectMapper()

            objectMapper.writeValue(response.writer, errorResponse)
        } catch (e: IOException) {

            logger.error(e) { "Failed to write error response" }
        }
    }
}
