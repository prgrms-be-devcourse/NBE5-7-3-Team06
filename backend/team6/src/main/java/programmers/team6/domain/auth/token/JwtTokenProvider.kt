package programmers.team6.domain.auth.token

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import programmers.team6.domain.auth.dto.JwtMemberInfo
import programmers.team6.domain.auth.dto.TokenBody
import programmers.team6.domain.auth.dto.TokenPairWithExpiration
import programmers.team6.domain.auth.dto.response.AccessTokenResponse
import programmers.team6.domain.auth.service.JwtService
import programmers.team6.domain.member.enums.Role
import programmers.team6.global.exception.code.UnauthorizedErrorCode
import programmers.team6.global.exception.customException.UnauthorizedException
import java.util.*
import javax.crypto.SecretKey


@Component
class JwtTokenProvider(
    private val jwtConfiguration: JwtConfiguration,
    private val jwtService: JwtService
) {

    companion object {
        private const val HEADER = "Authorization"
        private const val BEARER = "Bearer "
    }

    fun generateTokenPair(jwtMemberInfo: JwtMemberInfo): TokenPairWithExpiration {
        val accessToken = issueAccessToken(jwtMemberInfo)
        val refreshToken = issueRefreshToken(jwtMemberInfo)

        return TokenPairWithExpiration(
            accessToken, refreshToken, jwtConfiguration.accessTokenExpiration,
            jwtConfiguration.refreshTokenExpiration
        )
    }

    fun generateAccessToken(refreshToken: String): AccessTokenResponse {
        val tokenBody = parseClaims(refreshToken)

        val jwtMemberInfo = JwtMemberInfo(tokenBody.id, tokenBody.name, tokenBody.role)

        val accessToken = issueAccessToken(jwtMemberInfo)

        return AccessTokenResponse(accessToken, jwtConfiguration.accessTokenExpiration)
    }

    fun validate(token: String) {

        runCatching {
           Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
        }.onFailure { e ->
            throw when (e) {
                is SecurityException, is SignatureException ->
                    UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_INVALID_SIGNATURE)
                is MalformedJwtException ->
                    UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_MALFORMED_TOKEN)
                is ExpiredJwtException ->
                    UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_EXPIRED_TOKEN)
                is UnsupportedJwtException ->
                    UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_UNSUPPORTED_TOKEN)
                is IllegalArgumentException ->
                    UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_ILLEGAL_ARGUMENT_TOKEN)
                else ->
                    UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_INVALID_TOKEN)
            }
        }
    }

    fun validateNotBlackListed(refreshToken: String) {
        if (jwtService.isBlackListed(refreshToken)) {
            throw UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_BLACKLIST_TOKEN)
        }
    }

    fun parseClaims(token: String): TokenBody {
        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)

        val payload = claims.payload

        val id = payload.subject.toLong()

        return TokenBody(
            id,
            payload["name"].toString(),
            Role.valueOf(payload["role"].toString()),
            payload.expiration,
            payload.issuedAt
        )
    }

    fun issueAccessToken(jwtMemberInfo: JwtMemberInfo): String {
        return issue(jwtMemberInfo, jwtConfiguration.accessTokenExpiration)
    }

    fun issueRefreshToken(jwtMemberInfo: JwtMemberInfo): String {
        return issue(jwtMemberInfo, jwtConfiguration.refreshTokenExpiration)
    }

    private fun issue(jwtMemberInfo: JwtMemberInfo, expTime: Long): String {
        return Jwts.builder()
            .subject(jwtMemberInfo.id.toString())
            .claim("name", jwtMemberInfo.name)
            .claim("role", jwtMemberInfo.role)
            .issuedAt(Date())
            .expiration(Date(Date().time + expTime))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtConfiguration.secret.toByteArray())
    }

    fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader(HEADER)

        if (header != null && header.startsWith(BEARER)) {
            return header.substring(BEARER.length)
        }
        return null
    }


}
