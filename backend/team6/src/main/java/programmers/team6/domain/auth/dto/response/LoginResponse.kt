package programmers.team6.domain.auth.dto.response


data class LoginResponse(
    val authTokenResponse: AuthTokenResponse,
    val refreshToken: String,
    val refreshTokenExpiresIn: Long
)
