package programmers.team6.domain.auth.dto.response


data class LoginResponse(
    @JvmField val authTokenResponse: AuthTokenResponse,
    @JvmField val refreshToken: String,
    @JvmField val refreshTokenExpiresIn: Long
)
