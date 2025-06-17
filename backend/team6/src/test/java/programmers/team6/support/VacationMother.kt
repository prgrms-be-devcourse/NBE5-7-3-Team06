package programmers.team6.support

import programmers.team6.domain.admin.dto.response.ApprovalStepDetailUpdateResponse
import programmers.team6.domain.admin.dto.response.VacationRequestDetailReadResponse
import programmers.team6.domain.vacation.enums.ApprovalStatus
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import java.time.LocalDateTime

object VacationMother {

    fun defaultVacationDetail(memberId:Long = 0L): VacationRequestDetailReadResponse {

        val vacationRequestId = 0L
        val from = LocalDateTime.now()
        val to = from.plusDays(1)
        val name = "testName"
        val deptName = "testDeptName"
        val position = "testPosition"
        val reason = "testReason"
        val vacationType = "vacationType"
        val status = VacationRequestStatus.IN_PROGRESS

        return VacationRequestDetailReadResponse(
            vacationRequestId,
            from,
            to,
            memberId,
            name,
            deptName,
            position,
            reason,
            vacationType,
            status,
            emptyList()
        )
    }

    fun defaultApprovalStep(): ApprovalStepDetailUpdateResponse{
        val name = "testName"
        val reason = "testReason"
        val approvalStatus = ApprovalStatus.PENDING

        return ApprovalStepDetailUpdateResponse(name ,reason,approvalStatus)
    }

}
