package programmers.team6.domain.auth.token

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import programmers.team6.domain.auth.service.JwtService
import programmers.team6.domain.member.enums.Role
import programmers.team6.global.exception.code.UnauthorizedErrorCode
import programmers.team6.global.exception.customException.UnauthorizedException
import programmers.team6.support.JwtMemberInfoMother
import javax.crypto.SecretKey


internal class JwtTokenProviderTests {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var jwtConfiguration: JwtConfiguration
    private lateinit var jwtService: JwtService
    private lateinit var secretKey: SecretKey

    @BeforeEach
    fun setUp() {
        jwtService = mockk<JwtService>()

        jwtConfiguration = JwtConfiguration(
            "test-test-test-test-test-test-test-test-test-1234567890",
            300000L,
            1209600000,
            "Authorization",
            "Bearer"
        )
        jwtTokenProvider = JwtTokenProvider(jwtConfiguration, jwtService)

        secretKey = Keys.hmacShaKeyFor(jwtConfiguration.secret.toByteArray())
    }

    @Test
    @DisplayName("토큰을 정상적으로 생성한다.")
    fun generate_tokenPair() {



        val jwtMemberInfo = JwtMemberInfoMother.defaultUser()

        val tokenPair = jwtTokenProvider.generateTokenPair(jwtMemberInfo)

        val accessToken = genClaims(tokenPair.accessToken)

        val refreshToken = genClaims(tokenPair.refreshToken)

        Assertions.assertThat(accessToken.subject.toLong()).isEqualTo(jwtMemberInfo.id)
        Assertions.assertThat(accessToken["role"]).isEqualTo(Role.USER.name)
        Assertions.assertThat(accessToken.expiration).isNotNull()

        Assertions.assertThat(refreshToken.subject.toLong()).isEqualTo(jwtMemberInfo.id)
        Assertions.assertThat(refreshToken["role"]).isEqualTo(Role.USER.name)
        Assertions.assertThat(refreshToken.expiration).isNotNull()
    }


    @Test
    @DisplayName("access token 을 재발급한다.")
    fun generate_accessToken() {
        val jwtMemberInfo = JwtMemberInfoMother.defaultUser()

        val tokenPair = jwtTokenProvider.generateTokenPair(jwtMemberInfo)

        val refreshToken = tokenPair.refreshToken

        val accessTokenResponse = jwtTokenProvider.generateAccessToken(refreshToken)

        val claims = genClaims(accessTokenResponse.accessToken)

        Assertions.assertThat(claims.subject.toLong()).isEqualTo(jwtMemberInfo.id)
        Assertions.assertThat(claims["role"]).isEqualTo(Role.USER.name)
        Assertions.assertThat(claims.expiration).isNotNull()
    }

    @Test
    @DisplayName("토큰의 유효성을 검사한다 - 성공 ")
    fun token_validate_success() {
        val jwtMemberInfo = JwtMemberInfoMother.defaultUser()

        val tokenPair = jwtTokenProvider.generateTokenPair(jwtMemberInfo)

        val accessToken = tokenPair.accessToken

        Assertions.assertThatCode {
            jwtTokenProvider.validate(accessToken)
        }.doesNotThrowAnyException()
    }


    @Test
    @DisplayName("토큰 유효성 검사 실패 - 서명이 잘못된 토큰 ")
    fun token_validate_fail_signature() {
        val jwtMemberInfo = JwtMemberInfoMother.defaultUser()

        val wrongConfig = JwtConfiguration(
            "this-is-a-wrong-secret-key-which-is-very-long-32-bytes",
            jwtConfiguration.accessTokenExpiration,
            jwtConfiguration.refreshTokenExpiration,
            jwtConfiguration.header,
            jwtConfiguration.prefix
        )

        val wrongJwtTokenProvider = JwtTokenProvider(wrongConfig, jwtService)

        val tokenPair = wrongJwtTokenProvider.generateTokenPair(jwtMemberInfo)

        val wrongToken = tokenPair.accessToken

        Assertions.assertThatThrownBy {
            jwtTokenProvider.validate(wrongToken)
        }.isInstanceOf(UnauthorizedException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", UnauthorizedErrorCode.UNAUTHORIZED_INVALID_SIGNATURE)
    }

    @Test
    @DisplayName("토큰 유효성 검사 실패 - 구조가 잘못된 토큰 ")
    fun token_validate_fail_malformed() {
        val wrongToken = "not.jwt"

        Assertions.assertThatThrownBy {
            jwtTokenProvider.validate(wrongToken)
        }.isInstanceOf(UnauthorizedException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", UnauthorizedErrorCode.UNAUTHORIZED_MALFORMED_TOKEN)
    }

    @Test
    @DisplayName("토큰 유효성 검사 실패 - 만료된 토큰 ")
    fun token_validate_fail_expired() {
        val jwtMemberInfo = JwtMemberInfoMother.defaultUser()

        val wrongConfig = JwtConfiguration(
            jwtConfiguration.secret,
            1L,
            jwtConfiguration.refreshTokenExpiration,
            jwtConfiguration.header,
            jwtConfiguration.prefix
        )

        val wrongJwtTokenProvider = JwtTokenProvider(wrongConfig, jwtService)

        val tokenPair = wrongJwtTokenProvider.generateTokenPair(jwtMemberInfo)

        val wrongToken = tokenPair.accessToken

        Assertions.assertThatThrownBy {
            jwtTokenProvider.validate(wrongToken)
        }.isInstanceOf(UnauthorizedException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", UnauthorizedErrorCode.UNAUTHORIZED_EXPIRED_TOKEN)
    }

    @Test
    @DisplayName("토큰 유효성 검사 실패 - 빈 문자열")
    fun token_validate_fail_illegal() {
        Assertions.assertThatThrownBy {
            jwtTokenProvider.validate(" ")
        }.isInstanceOf(UnauthorizedException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", UnauthorizedErrorCode.UNAUTHORIZED_ILLEGAL_ARGUMENT_TOKEN)
    }

    @Test
    @DisplayName("refresh token이 블랙리스트에 등록되어있지 않으면 아무것도 반환하지않는다.")
    fun validate_not_blackListed_success() {
        val refreshToken = "refreshToken"

        every{
            jwtService.isBlackListed(refreshToken)
        }returns false

        Assertions.assertThatCode {
            jwtTokenProvider.validateNotBlackListed(refreshToken)
        }.doesNotThrowAnyException()
    }

    @Test
    @DisplayName("refresh token이 블랙리스트에 등록되어있으면 예외를 반환한다.")
    fun validate_not_blackListed_failure() {
        val expiredToken = "refreshToken"

        Mockito.`when`(jwtService.isBlackListed(expiredToken)).thenReturn(true)

        Assertions.assertThatThrownBy {
            jwtTokenProvider.validateNotBlackListed(expiredToken)
        }.isInstanceOf(UnauthorizedException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", UnauthorizedErrorCode.UNAUTHORIZED_BLACKLIST_TOKEN)
    }


    @Test
    @DisplayName("token을 넣으면 token body를 반환한다.")
    fun parse_claims() {
        val jwtMemberInfo = JwtMemberInfoMother.defaultUser()
        val tokenPair = jwtTokenProvider.generateTokenPair(jwtMemberInfo)
        val token = tokenPair.accessToken

        val tokenBody = jwtTokenProvider.parseClaims(token)

        Assertions.assertThat(tokenBody)
            .extracting("id", "name", "role")
            .containsExactly(
                jwtMemberInfo.id,
                jwtMemberInfo.name,
                jwtMemberInfo.role
            )
        Assertions.assertThat(tokenBody.expiration).isNotNull()
        Assertions.assertThat(tokenBody.issuedAt).isNotNull()
    }

    @Test
    @DisplayName("토큰 발급하기")
    fun issue_token() {
        val jwtMemberInfo = JwtMemberInfoMother.defaultUser()

        val accessToken = jwtTokenProvider.issueAccessToken(jwtMemberInfo)

        val claims = genClaims(accessToken)

        Assertions.assertThat(claims.subject.toLong()).isEqualTo(jwtMemberInfo.id)
        Assertions.assertThat(claims["name"]).isEqualTo(jwtMemberInfo.name)
        Assertions.assertThat(claims["role"]).isEqualTo(jwtMemberInfo.role.name)

        Assertions.assertThat(claims.expiration).isNotNull()
        Assertions.assertThat(claims.issuedAt).isNotNull()
    }

    @Test
    @DisplayName("request 에서 token 추출하기 - 성공")
    fun extract_token_success() {
        val request = Mockito.mock(
            HttpServletRequest::class.java
        )
        val token = "this.is.token"
        val header = "Bearer $token"

        Mockito.`when`(request.getHeader("Authorization")).thenReturn(header)

        val extractedToken = jwtTokenProvider.extractToken(request)

        Assertions.assertThat(extractedToken).isEqualTo(token)
    }

    @Test
    @DisplayName("request 에서 token 추출하기 - 실패하면 null 반환")
    fun extract_token_failure() {
        val request = Mockito.mock(
            HttpServletRequest::class.java
        )
        val token = "this.is.token"
        val wrongHeader = "wrong$token"

        Mockito.`when`(request.getHeader("Authorization")).thenReturn(wrongHeader)

        val extractedToken = jwtTokenProvider.extractToken(request)

        Assertions.assertThat(extractedToken).isNull()
    }

    private fun genClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}