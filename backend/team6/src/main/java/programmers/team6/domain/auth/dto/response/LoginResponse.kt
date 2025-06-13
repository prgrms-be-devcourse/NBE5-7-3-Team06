package programmers.team6.domain.auth.dto.response;

public record LoginResponse(
	AuthTokenResponse authTokenResponse,
	String refreshToken,
	long refreshTokenExpiresIn
) {
}
