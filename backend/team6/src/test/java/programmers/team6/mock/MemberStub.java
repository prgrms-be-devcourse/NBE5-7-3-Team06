package programmers.team6.mock;

import java.time.LocalDateTime;

import programmers.team6.domain.member.entity.Code;
import programmers.team6.domain.member.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.enums.Role;

public class MemberStub extends Member {

	private Long id;

	public MemberStub(Long id, String name, Dept dept, Code position, LocalDateTime joinDate, Role role) {
		super(name, dept, position, joinDate, role);
		this.id = id;
	}

	@Override
	public Long getId() {
		return this.id;
	}

	public static MemberStubBuilder subBuilder() {
		return new MemberStubBuilder();
	}

	public static class MemberStubBuilder {
		private Long id;
		private String name;
		private Dept dept;
		private Code position;
		private LocalDateTime joinDate;
		private Role role;

		public MemberStubBuilder id(Long id) {
			this.id = id;
			return this;
		}

		public MemberStubBuilder name(String name) {
			this.name = name;
			return this;
		}

		public MemberStubBuilder dept(Dept dept) {
			this.dept = dept;
			return this;
		}

		public MemberStubBuilder position(Code position) {
			this.position = position;
			return this;
		}

		public MemberStubBuilder joinDate(LocalDateTime joinDate) {
			this.joinDate = joinDate;
			return this;
		}

		public MemberStubBuilder role(Role role) {
			this.role = role;
			return this;
		}

		public Member build() {
			return new MemberStub(id, name, dept, position, joinDate, role);
		}
	}
}
