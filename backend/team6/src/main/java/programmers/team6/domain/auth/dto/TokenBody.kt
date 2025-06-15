package programmers.team6.domain.auth.dto;

import java.util.Date;

import programmers.team6.domain.member.enums.Role;

public record TokenBody(
	Long id,
	String name,
	Role role,
	Date expiration,
	Date issuedAt
) {
}
