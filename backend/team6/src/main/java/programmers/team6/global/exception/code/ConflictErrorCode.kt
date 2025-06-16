package programmers.team6.global.exception.code

import org.springframework.http.HttpStatus
import programmers.team6.global.exception.ErrorStatus

enum class ConflictErrorCode(
    override val message: String
) : ErrorCode {

    CONFLICT_EMAIL("중복된 이메일입니다."),
    CONFLICT_APPROVAL_STEP("결재 단계 동기화 실패"),
    CONFLICT_VERSION("버전이 맞지 않습니다");

    override val httpStatus: HttpStatus = HttpStatus.CONFLICT

    override val errorStatus: ErrorStatus
        get() = ErrorStatus.CONFLICT

    override val httpStatusCode: Int
        get() = httpStatus.value()
}
