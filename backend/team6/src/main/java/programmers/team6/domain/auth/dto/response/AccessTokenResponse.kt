package programmers.team6.domain.auth.dto.response;

public record AccessTokenResponse(
	String accessToken,
	long accessTokenExpiresIn
) {
}