package programmers.team6.domain.auth.dto;

import programmers.team6.domain.member.enums.Role;

public record JwtMemberInfo(Long id, String name, Role role) {
}
