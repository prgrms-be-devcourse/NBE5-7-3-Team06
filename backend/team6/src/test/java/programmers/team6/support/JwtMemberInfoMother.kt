package programmers.team6.support

import programmers.team6.domain.auth.dto.JwtMemberInfo
import programmers.team6.domain.member.enums.Role

object JwtMemberInfoMother {
    @JvmStatic
    fun defaultUser(): JwtMemberInfo {
        return JwtMemberInfo(1L, "member1", Role.USER)
    }

    fun admin(): JwtMemberInfo {
        return JwtMemberInfo(2L, "admin", Role.ADMIN)
    }
}
