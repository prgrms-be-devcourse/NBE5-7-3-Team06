package programmers.team6.domain.vacation.util.mapper

import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.dto.response.ApprovalFirstStepDetailResponse
import programmers.team6.domain.vacation.dto.response.ApprovalSecondStepDetailResponse
import programmers.team6.domain.vacation.entity.ApprovalStep
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.ApprovalStatus

object ApprovalStepMapper {
    fun fromFirstStepEntity(approvalStep: ApprovalStep): ApprovalFirstStepDetailResponse {
        return ApprovalFirstStepDetailResponse(
            approvalStepId = approvalStep.id!!,
            name = approvalStep.vacationRequest.member.name,
            deptName = approvalStep.vacationRequest.member.dept!!.deptName,
            positionName = approvalStep.vacationRequest.member.position.name,
            status = approvalStep.approvalStatus,
            type = approvalStep.vacationRequest.type.name,
            from = approvalStep.vacationRequest.from,
            to = approvalStep.vacationRequest.to,
            reason = approvalStep.vacationRequest.reason,
            approverName = approvalStep.member.name,
            approvalReason = approvalStep.reason
        )
    }

    fun fromSecondStepEntity(approvalStep: ApprovalStep): ApprovalSecondStepDetailResponse {
        return ApprovalSecondStepDetailResponse(
            approvalStepId = approvalStep.id!!,
            name = approvalStep.vacationRequest.member.name,
            deptName = approvalStep.vacationRequest.member.dept!!.deptName,
            positionName = approvalStep.vacationRequest.member.position.name,
            status = approvalStep.approvalStatus,
            type = approvalStep.vacationRequest.type.name,
            from = approvalStep.vacationRequest.from,
            to = approvalStep.vacationRequest.to,
            reason = approvalStep.vacationRequest.reason,
            approverName = approvalStep.member.name,
            approvalReason = approvalStep.reason
        )
    }

    fun toEntity(member: Member, vacationRequest: VacationRequest, step: Int): ApprovalStep {
        val approvalStatus = if (step == 1) ApprovalStatus.PENDING else ApprovalStatus.WAITING
        return ApprovalStep(
            member = member,
            vacationRequest = vacationRequest,
            step = step,
            approvalStatus = approvalStatus
        )
    }
}
