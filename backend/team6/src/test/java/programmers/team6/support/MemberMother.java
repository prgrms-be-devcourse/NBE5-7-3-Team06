package programmers.team6.support;

import java.time.LocalDateTime;

import programmers.team6.domain.member.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.enums.Role;
import programmers.team6.mock.MemberStub;

public class MemberMother {

	public static Member withId(Long id) {
		return MemberStub.subBuilder()
			.id(id)
			.name("testMember")
			.dept(new Dept("testDept", null))
			.position(PositionMother.employee())
			.joinDate(LocalDateTime.of(2025, 6, 12, 10, 11))
			.role(Role.USER)
			.build();
	}

	public static Member withIdAndDeptName(Long id, String deptName) {
		return MemberStub.subBuilder()
			.id(id)
			.name("testMember" + id)
			.dept(new Dept(deptName, null))
			.position(PositionMother.employee())
			.joinDate(LocalDateTime.of(2025, 6, 12, 10, 11))
			.role(Role.USER)
			.build();
  }
	
  public static Member member() {
		return withId(0L);
	}
}
