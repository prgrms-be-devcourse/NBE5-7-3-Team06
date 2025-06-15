package programmers.team6.domain.member.util.mapper

import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.auth.dto.request.MemberSignUpRequest
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.entity.MemberInfo
import programmers.team6.domain.member.enums.Role

object MemberMapper {

    fun toEntity(
        memberSignUpRequest: MemberSignUpRequest,
        dept: Dept,
        position: Code,
        encodedPassword: String
    ): Member {

        val memberInfo: MemberInfo = MemberInfo(memberSignUpRequest.birth,memberSignUpRequest.email,encodedPassword)

        val member = Member(memberSignUpRequest.name,dept,position,memberSignUpRequest.joinDate,Role.PENDING)

        member.memberInfo = memberInfo

        return member
    }
}
