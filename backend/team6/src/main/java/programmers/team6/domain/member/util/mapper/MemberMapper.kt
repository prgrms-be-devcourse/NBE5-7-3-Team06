package programmers.team6.domain.member.util.mapper

import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.auth.dto.request.MemberSignUpRequest
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.entity.MemberInfo
import programmers.team6.domain.member.enums.Role

object MemberMapper {
    fun MemberCreateRequestToEntity(
        memberSignUpRequest: MemberSignUpRequest,
        dept: Dept,
        position: Code,
        encodedPassword: String
    ): Member {
        val memberInfo = MemberInfo.builder()
            .birth(memberSignUpRequest.birth)
            .email(memberSignUpRequest.email)
            .password(encodedPassword)
            .build()

        val member = Member.builder()
            .name(memberSignUpRequest.name)
            .dept(dept)
            .position(position)
            .joinDate(memberSignUpRequest.joinDate)
            .role(Role.PENDING)
            .build()

        member.memberInfo = memberInfo

        return member
    }
}
