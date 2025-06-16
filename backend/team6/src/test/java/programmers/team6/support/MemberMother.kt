package programmers.team6.support

import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.entity.MemberInfo
import programmers.team6.domain.member.enums.Role
import programmers.team6.mock.MemberStub
import programmers.team6.support.PositionMother.employee
import java.time.LocalDateTime

object MemberMother {

    fun withId(id: Long): Member {
        val member = MemberStub.subBuilder()
            .id(id)
            .name("testMember")
            .dept(Dept(null, "testDept", null))
            .position(employee())
            .joinDate(LocalDateTime.of(2025, 6, 12, 10, 11))
            .role(Role.USER)
            .build()
        member.memberInfo = MemberInfo("birth", "test@gmail.com", "testPassword")
        return member
    }

    fun withIdAndRole(id: Long, role: Role): Member {
        return MemberStub.subBuilder()
            .id(id)
            .name("testMember")
            .dept(Dept(null, "testDept", null))
            .position(employee())
            .joinDate(LocalDateTime.of(2025, 6, 12, 10, 11))
            .role(role)
            .build()
    }

    fun withIdAndDeptName(id: Long, deptName: String): Member {
        return MemberStub.subBuilder()
            .id(id)
            .name("testMember$id")
            .dept(Dept(null, deptName, null))
            .position(employee())
            .joinDate(LocalDateTime.of(2025, 6, 12, 10, 11))
            .role(Role.USER)
            .build()
    }

    fun member(): Member {
        return withId(0L)
    }
}
