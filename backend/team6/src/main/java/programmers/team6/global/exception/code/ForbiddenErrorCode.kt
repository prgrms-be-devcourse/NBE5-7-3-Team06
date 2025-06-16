package programmers.team6.global.exception.code

import org.springframework.http.HttpStatus
import programmers.team6.global.exception.ErrorStatus

enum class ForbiddenErrorCode(override val message: String) : ErrorCode {
    FORBIDDEN_PENDING("회원가입 승인 대기중입니다"),
    FORBIDDEN_NO_AUTHORITY("권한이 불충분합니다.");

    override val httpStatus: HttpStatus = HttpStatus.FORBIDDEN

    override val errorStatus: ErrorStatus
        get() = ErrorStatus.FORBIDDEN

    override val httpStatusCode: Int
        get() = httpStatus.value()
}
