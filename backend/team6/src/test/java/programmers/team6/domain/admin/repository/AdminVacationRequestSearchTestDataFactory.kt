package programmers.team6.domain.admin.repository

import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.enums.Role
import programmers.team6.domain.vacation.entity.ApprovalStep
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.ApprovalStatus
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.support.MemberMother
import java.time.LocalDateTime

object AdminVacationRequestSearchTestDataFactory {
    fun genTestApprovalStep(vacationRequest: VacationRequest, step: Int, reason: String): ApprovalStep {
        return ApprovalStep(
            vacationRequest = vacationRequest,
            member = MemberMother.withId(1),
            step = step,
            approvalStatus = ApprovalStatus.PENDING,
            reason = reason
        )
    }

    fun genTestCode(groupCode: String, code: String, name: String): Code {
        return Code(groupCode, code, name)
    }

    fun genTestCodeList(groupCode: String, cnt: Int, prefixName: String): MutableList<Code> {
        val result: MutableList<Code> = mutableListOf()
        for (i in 0..cnt) {
            result.add(genTestCode(groupCode, String.format("%02d", i), String.format("%s%d", prefixName, i)))
        }
        return result
    }

    fun genTestDept(deptName: String): Dept {
        return Dept(null, deptName, null)
    }

    fun genTestDeptList(cnt: Int, prefixDeptName: String): MutableList<Dept> {
        val result: MutableList<Dept> = mutableListOf()
        for (i in 0..<cnt) {
            result.add(genTestDept(String.format("%s%d", prefixDeptName, i)))
        }
        return result
    }

    fun genTestMember(name: String, dept: Dept, positionCode: Code): Member {
        return Member(name, dept, positionCode, LocalDateTime.now(), Role.USER)
    }

    fun genTestMemberList(
        cnt: Int,
        startName: Char,
        depts: List<Dept>,
        positionCodes: List<Code>
    ): MutableList<Member> {
        val result: MutableList<Member> = mutableListOf()
        for (i in 0..<cnt) {
            result.add(
                genTestMember(
                    String.format("%s%d", (startName.code + i).toChar(), i),
                    depts.get(i),
                    positionCodes.get(i)
                )
            )
        }
        return result
    }

    fun genTestMemberList(
        cnt: Int,
        prefixName: String,
        depts: List<Dept>,
        positionCode: Code
    ): MutableList<Member> {
        val result: MutableList<Member> = mutableListOf()
        for (i in 0..<cnt) {
            result.add(genTestMember(String.format("%s%d", prefixName, i), depts.get(i), positionCode))
        }
        return result
    }

    fun genVacationRequest(
        member: Member, start: LocalDateTime, end: LocalDateTime,
        reason: String, type: Code, status: VacationRequestStatus
    ): VacationRequest {
        return VacationRequest(member, start, end, reason, type, status, 0)
    }

    fun genApprovalStep(
        step: Int, approvalStatus: ApprovalStatus, member: Member,
        vacationRequest: VacationRequest
    ): ApprovalStep {
        return ApprovalStep(
            vacationRequest = vacationRequest,
            member = member,
            step = step,
            approvalStatus = approvalStatus,
            reason = ""
        )
    }
}
