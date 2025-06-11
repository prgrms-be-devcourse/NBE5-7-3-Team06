package programmers.team6.domain.auth.dto.response;

import programmers.team6.domain.member.enums.Role;

public record AuthTokenResponse(
	String accessToken,
	long accessTokenExpiresIn,
	Long id,
	String name,
	Role role
) {
}
