package programmers.team6.domain.vacation.service.util

import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.entity.ApprovalStep
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.ApprovalStatus
import programmers.team6.support.VacationTypeMother
import java.time.LocalDateTime

object ApprovalStepServiceUtils {
    fun genVacationRequest(member: Member): VacationRequest {
        return VacationRequest(
            from = LocalDateTime.of(2025, 8, 1, 9, 0),
            to = LocalDateTime.of(2025, 8, 3, 18, 0),
            member = member,
            type = VacationTypeMother.Annual(),
            reason = "사정이 있습니다."
        )
    }

    fun genFirstStep(id: Long, approver: Member, vacationRequest: VacationRequest): ApprovalStep {
        return ApprovalStep(
            id = id, member = approver, vacationRequest = vacationRequest,
            approvalStatus = ApprovalStatus.PENDING, step = 1
        )
    }

    fun genFirstStep(
        id: Long, approver: Member, vacationRequest: VacationRequest, status: ApprovalStatus
    ): ApprovalStep {
        return ApprovalStep(
            id = id, member = approver, vacationRequest = vacationRequest,
            approvalStatus = status, 1
        )
    }

    fun genSecondStep(id: Long, approver: Member, vacationRequest: VacationRequest): ApprovalStep {
        return ApprovalStep(
            id = id, member = approver, vacationRequest = vacationRequest,
            approvalStatus = ApprovalStatus.WAITING, 2
        )
    }

    fun genSecondStep(
        id: Long, approver: Member, vacationRequest: VacationRequest, status: ApprovalStatus
    ): ApprovalStep {
        return ApprovalStep(
            id = id, member = approver, vacationRequest = vacationRequest,
            approvalStatus = status, 2
        )
    }
}
