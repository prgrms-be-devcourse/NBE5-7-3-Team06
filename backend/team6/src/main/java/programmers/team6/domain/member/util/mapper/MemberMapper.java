package programmers.team6.domain.member.util.mapper;

import programmers.team6.domain.auth.dto.request.MemberSignUpRequest;
import programmers.team6.domain.admin.entity.Code;
import programmers.team6.domain.admin.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.entity.MemberInfo;
import programmers.team6.domain.member.enums.Role;

public class MemberMapper {

	public static Member MemberCreateRequestToEntity(MemberSignUpRequest memberSignUpRequest, Dept dept,
		Code position, String encodedPassword) {

		MemberInfo memberInfo = MemberInfo.builder()
			.birth(memberSignUpRequest.birth())
			.email(memberSignUpRequest.email())
			.password(encodedPassword)
			.build();

		Member member = Member.builder()
			.name(memberSignUpRequest.name())
			.dept(dept)
			.position(position)
			.joinDate(memberSignUpRequest.joinDate())
			.role(Role.PENDING)
			.build();

		member.setMemberInfo(memberInfo);

		return member;
	}

}
