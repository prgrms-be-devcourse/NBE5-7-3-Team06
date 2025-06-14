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
            approvalStep.id,
            approvalStep.vacationRequest.member.name,
            approvalStep.vacationRequest.member.dept.deptName,
            approvalStep.vacationRequest.member.position.name,
            approvalStep.approvalStatus,
            approvalStep.vacationRequest.type.name,
            approvalStep.vacationRequest.from,
            approvalStep.vacationRequest.to,
            approvalStep.vacationRequest.reason,
            approvalStep.member.name,
            approvalStep.reason
        )
    }

    fun fromSecondStepEntity(approvalStep: ApprovalStep): ApprovalSecondStepDetailResponse {
        return ApprovalSecondStepDetailResponse(
            approvalStep.id,
            approvalStep.vacationRequest.member.name,
            approvalStep.vacationRequest.member.dept.deptName,
            approvalStep.vacationRequest.member.position.name,
            approvalStep.approvalStatus,
            approvalStep.vacationRequest.type.name,
            approvalStep.vacationRequest.from,
            approvalStep.vacationRequest.to,
            approvalStep.vacationRequest.reason,
            approvalStep.member.name,
            approvalStep.reason
        )
    }

    fun toEntity(member: Member, vacationRequest: VacationRequest, step: Int): ApprovalStep {
        val approvalStatus = if (step == 1) ApprovalStatus.PENDING else ApprovalStatus.WAITING
        return ApprovalStep.builder()
            .member(member)
            .vacationRequest(vacationRequest)
            .step(step)
            .approvalStatus(approvalStatus)
            .build()
    }
}
