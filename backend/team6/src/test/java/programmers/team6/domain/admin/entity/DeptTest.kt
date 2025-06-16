package programmers.team6.domain.admin.entity

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import programmers.team6.domain.member.entity.Member
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.NotFoundException

internal class DeptTest {
    @Test
    @DisplayName("부서장이 제공되었을때, 성공적으로 부서의 부서장으로 지정")
    fun should_successAppointLeader_when_givenLeaderMember() {
        // given
        val dept = Dept(null, "deptName", null)
        val leader = Member.builder().build()

        // when
        dept.appointLeader(leader)

        // then
        Assertions.assertThat(dept.deptLeader).isEqualTo(leader)
    }

    @Test
    @DisplayName("부서장이 null일 경우, NotFoundException 발생")
    fun should_throwNotFoundException_when_deptLeaderIsNull() {
        // given & when
        val dept = Dept(null, "deptName", null)

        // then
        Assertions.assertThatThrownBy { dept.deptLeaderOrThrow() }.isInstanceOf(
            NotFoundException::class.java
        ).hasMessage(
            NotFoundErrorCode.NOT_FOUND_DEPT_LEADER.message
        )
    }
}