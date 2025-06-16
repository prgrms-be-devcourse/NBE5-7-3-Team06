package programmers.team6.global.exception.code

import org.springframework.http.HttpStatus
import programmers.team6.global.exception.ErrorStatus

enum class NotFoundErrorCode(override val message: String) : ErrorCode {
    NOT_FOUND_DEPT("부서 정보를 찾을 수 없습니다."),
    NOT_FOUND_POSITION("직위 정보를 찾을 수 없습니다."),
    NOT_FOUND_EMAIL("이메일 정보를 찾을 수 없습니다."),
    NOT_FOUND_MEMBER("멤버 정보를 찾을 수 없습니다."),
    NOT_FOUND_VACATION_REQUEST("휴가계를 찾을 수 없습니다."),
    NOT_FOUND_APPROVAL_STEP("결재 단계를 찾을 수 없습니다."),
    NOT_FOUND_CODE("존재하지 않는 코드입니다."),
    NOT_FOUND_VACATION_INFO("휴가 정보를 찾을 수 없습니다."),
    NOT_FOUND_DEPT_LEADER("부서장 정보를 찾을 수 없습니다."),
    NOT_FOUND_REMAIN_VACATION("사용 가능한 휴가 일수가 없습니다.");

    override val httpStatus: HttpStatus = HttpStatus.NOT_FOUND

    override val errorStatus: ErrorStatus
        get() = ErrorStatus.NOT_FOUND

    override val httpStatusCode: Int
        get() = httpStatus.value()
}
