package programmers.team6.global.exception.code

import org.springframework.http.HttpStatus
import programmers.team6.global.exception.ErrorStatus

interface ErrorCode {
    val errorStatus: ErrorStatus

    val httpStatus: HttpStatus

    val httpStatusCode: Int

    val message: String
}
