package programmers.team6.domain.auth.dto;

public record TokenPairWithExpiration(
	String accessToken,
	String refreshToken,
	long accessTokenExpiresIn,
	long refreshTokenExpiresIn
) {
}

