package programmers.team6.domain.vacation.util.mapper

import lombok.AccessLevel
import lombok.NoArgsConstructor
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.dto.response.ApprovalFirstStepDetailResponse
import programmers.team6.domain.vacation.dto.response.ApprovalSecondStepDetailResponse
import programmers.team6.domain.vacation.entity.ApprovalStep
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.ApprovalStatus

@NoArgsConstructor(access = AccessLevel.PRIVATE)
object ApprovalStepMapper {
    fun fromFirstStepEntity(approvalStep: ApprovalStep): ApprovalFirstStepDetailResponse {
        return ApprovalFirstStepDetailResponse(
            approvalStepId = approvalStep.id!!,
            name = approvalStep.vacationRequest.getMember().getName(),
            deptName = approvalStep.vacationRequest.getMember().getDept().getDeptName(),
            positionName = approvalStep.vacationRequest.getMember().getPosition().getName(),
            status = approvalStep.approvalStatus,
            type = approvalStep.vacationRequest.getType().getName(),
            from = approvalStep.vacationRequest.getFrom(),
            to = approvalStep.vacationRequest.getTo(),
            reason = approvalStep.vacationRequest.getReason(),
            approverName = approvalStep.member.getName(),
            approvalReason = approvalStep.reason
        )
    }

    fun fromSecondStepEntity(approvalStep: ApprovalStep): ApprovalSecondStepDetailResponse {
        return ApprovalSecondStepDetailResponse(
            approvalStepId = approvalStep.id!!,
            name = approvalStep.vacationRequest.getMember().getName(),
            deptName = approvalStep.vacationRequest.getMember().getDept().getDeptName(),
            positionName = approvalStep.vacationRequest.getMember().getPosition().getName(),
            status = approvalStep.approvalStatus,
            type = approvalStep.vacationRequest.getType().getName(),
            from = approvalStep.vacationRequest.getFrom(),
            to = approvalStep.vacationRequest.getTo(),
            reason = approvalStep.vacationRequest.getReason(),
            approverName = approvalStep.member.getName(),
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
