package programmers.team6.domain.vacation.entity.util

import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.enums.Role
import programmers.team6.domain.vacation.entity.ApprovalStep
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.ApprovalStatus
import programmers.team6.support.MemberMother
import programmers.team6.support.TestVacationType
import java.time.LocalDateTime

object ApprovalStepTestUtils {
    fun genApprovalStep(status: ApprovalStatus): ApprovalStep {
        return ApprovalStep(
            step = 1,
            member = Member(
                name = "name",
                dept = null,
                position = Code("test", "test", "test"),
                joinDate = LocalDateTime.now(),
                Role.USER
            ),
            approvalStatus = status,
            vacationRequest = VacationRequest(
                MemberMother.withId(0L), LocalDateTime.now(), LocalDateTime.now().plusDays(1), "",
                TestVacationType.ANNUAL.toCode()
            )

        )
    }
}
