package programmers.team6.support;

import programmers.team6.domain.auth.dto.JwtMemberInfo;
import programmers.team6.domain.member.enums.Role;

public class JwtMemberInfoMother {

    public static JwtMemberInfo defaultUser() {
        return new JwtMemberInfo(1L, "member1", Role.USER);
    }

    public static JwtMemberInfo admin() {
        return new JwtMemberInfo(2L, "admin", Role.ADMIN);
    }

}
