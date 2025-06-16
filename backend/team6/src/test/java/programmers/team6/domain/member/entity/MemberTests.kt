package programmers.team6.domain.member.entity

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.member.enums.Role
import programmers.team6.global.exception.code.BadRequestErrorCode
import programmers.team6.global.exception.customException.BadRequestException
import java.time.LocalDateTime

internal class MemberTests {

    @Test
    @DisplayName("멤버 엔티티 권한을 승인으로 업데이트한다.")
    fun member_approve_success() {

        val member = createMember(role = Role.PENDING);

        member.approve()

        Assertions.assertThat(member.role).isEqualTo(Role.USER)
    }


    @Test
    @DisplayName("멤버 엔티티 권한 업데이트 시 대기중이 아니면 실패 ")
    fun member_approve_fail() {
        
        val member = createMember()

        Assertions.assertThatThrownBy {
            member.approve()
        }.isInstanceOf(BadRequestException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", BadRequestErrorCode.BAD_REQUEST_MEMBER_ROLE)
    }

    @Test
    @DisplayName("멤버 엔티티의 권한이 대기중이 아니면 예외를 반환한다")
    fun validation_deletable_on_reject() {

        val member = createMember()

        Assertions.assertThatThrownBy {
            member.validateDeletableOnReject()
        }.isInstanceOf(BadRequestException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", BadRequestErrorCode.BAD_REQUEST_MEMBER_ROLE)
    }

    @Test
    @DisplayName("멤버 엔티티의 부서정보가 비어있지 않고 부서가 인사팀이면 true를 반환한다.")
    fun is_hr_member_true() {

        val hrDept = defaultDept(name = "인사팀")
        val hrMember = createMember(dept = hrDept)

        Assertions.assertThat(hrMember.isHrMember).isTrue()
    }

    @Test
    @DisplayName("멤버 엔티티의 부서가 인사팀이 아니면 false를  반환한다.")
    fun is_hr_member_not_hrTeam_false() {

        val member = createMember()

        Assertions.assertThat(member.isHrMember).isFalse()
    }


    @Test
    @DisplayName("멤버 엔티티의 부서가 null이면 false를  반환한다.")
    fun is_hr_member_dept_null_false() {

        val member = createMember(dept = null)

        Assertions.assertThat(member.isHrMember).isFalse()
    }


    private fun defaultPosition() = Code("POSITION", "01", "사원")

    private fun defaultDept(name: String = "개발팀") = Dept(1L, name, null)

    private fun defaultJoinDate() = LocalDateTime.of(2024, 1, 1, 12, 0)

    private fun createMember(
        name: String = "member1",
        dept: Dept? = defaultDept(),
        position: Code = defaultPosition(),
        joinDate: LocalDateTime = defaultJoinDate(),
        role: Role = Role.USER
    ): Member {
        return Member(name, dept, position, joinDate, role)
    }

}
