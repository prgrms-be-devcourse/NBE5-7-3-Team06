package programmers.team6.domain.admin.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.customException.NotFoundException;

class DeptTest {
	@Test
	@DisplayName("부서장이 제공되었을때, 성공적으로 부서의 부서장으로 지정")
	void should_successAppointLeader_when_givenLeaderMember() {
		// given
		Dept dept = Dept.builder().deptName("testDeptName").build();
		Member leader = Member.builder().build();

		// when
		dept.appointLeader(leader);

		// then
		assertThat(dept.getDeptLeader()).isEqualTo(leader);
	}

	@Test
	@DisplayName("부서장이 null일 경우, NotFoundException 발생")
	void should_throwNotFoundException_when_deptLeaderIsNull() {
		// given & when
		Dept dept = Dept.builder().deptName("testDeptName").build();

		// then
		assertThatThrownBy(() -> dept.getDeptLeader()).isInstanceOf(NotFoundException.class).hasMessage(
			NotFoundErrorCode.NOT_FOUND_DEPT_LEADER.getMessage());
	}
}