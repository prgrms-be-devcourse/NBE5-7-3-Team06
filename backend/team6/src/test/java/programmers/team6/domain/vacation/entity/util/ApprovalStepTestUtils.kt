package programmers.team6.domain.vacation.entity.util

import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.enums.Role
import programmers.team6.domain.vacation.entity.ApprovalStep
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.ApprovalStatus
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.support.MemberMother
import programmers.team6.support.VacationTypeMother
import java.time.LocalDateTime

object ApprovalStepTestUtils {
    fun genApprovalStep(status: ApprovalStatus): ApprovalStep {
        return ApprovalStep(
            step = 1,
            member = Member(
                name = "name",
                dept = defaultDept(),
                position = Code("test", "test", "test"),
                joinDate = LocalDateTime.now(),
                Role.USER
            ),
            approvalStatus = status,
            vacationRequest = defaultVacationRequest()

        )
    }

    private fun defaultDept(name: String = "개발팀") = Dept(1L, name, null)

    private fun defaultVacationRequest() = VacationRequest(
        from = LocalDateTime.of(2024, 1, 1, 1, 1),
        to = LocalDateTime.of(2024, 1, 1, 1, 1),
        member = MemberMother.withId(1L),
        type = VacationTypeMother.Annual(),
        status = VacationRequestStatus.APPROVED,
        reason = ""
    )
}
