package programmers.team6.domain.member.service

import org.springframework.stereotype.Service
import programmers.team6.domain.admin.dto.response.VacationRequestDetailReadResponse
import programmers.team6.domain.vacation.support.VacationRequestReader
import programmers.team6.global.exception.code.ForbiddenErrorCode
import programmers.team6.global.exception.customException.ForbiddenException

@Service
class MemberVacationRequestService(val vacationRequestReader: VacationRequestReader) {

    fun selectVacationRequestDetailById(vacationRequestId: Long, memberId: Long): VacationRequestDetailReadResponse {
        val details = vacationRequestReader.readDetailFrom(vacationRequestId)
        if (memberId != details.memberId) {
            throw ForbiddenException(ForbiddenErrorCode.FORBIDDEN_NO_AUTHORITY)
        }
        return details
    }
}
