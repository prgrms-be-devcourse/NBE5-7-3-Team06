package programmers.team6.domain.auth.token

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import programmers.team6.domain.auth.dto.TokenBody
import programmers.team6.global.exception.code.UnauthorizedErrorCode
import programmers.team6.global.exception.customException.UnauthorizedException
import programmers.team6.global.exception.response.ErrorResponse
import programmers.team6.support.JwtMemberInfoMother
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class JwtAuthenticationFilterTest {
    @InjectMocks
    private val jwtAuthenticationFilter: JwtAuthenticationFilter? = null

    @Mock
    private val jwtTokenProvider: JwtTokenProvider? = null

    @Mock
    private val filterChain: MockFilterChain? = null

    private var request: MockHttpServletRequest? = null
    private var response: MockHttpServletResponse? = null

    private var objectMapper: ObjectMapper? = null


    @BeforeEach
    fun setUp() {
        SecurityContextHolder.clearContext()

        request = MockHttpServletRequest()
        response = MockHttpServletResponse()

        objectMapper = Jackson2ObjectMapperBuilder.json().build()
    }

    @Test
    @DisplayName("인증이 필요 없는 uri로 접근하면 doFilter로 넘어간다 ")
    @Throws(Exception::class)
    fun access_with_freeToken() {
        request!!.requestURI = "/codes/group/POSITION"

        jwtAuthenticationFilter!!.doFilterInternal(request!!, response!!, filterChain!!)

        Mockito.verify(filterChain, Mockito.times(1)).doFilter(request, response)
    }

    @Test
    @DisplayName("정상적인 token은 필터를 통과한다.")
    @Throws(Exception::class)
    fun valid_token_success() {
        val jwtMemberInfo = JwtMemberInfoMother.defaultUser()
        val token = "token"

        val tokenBody = TokenBody(
            jwtMemberInfo.id,
            jwtMemberInfo.name,
            jwtMemberInfo.role,
            Date(System.currentTimeMillis() + 1000 * 60 * 60),
            Date()
        )

        request!!.requestURI = "/vacations"

        request!!.addHeader("Authorization", "Bearer $token")

        Mockito.`when`(jwtTokenProvider!!.extractToken(request!!)).thenReturn(token)
        Mockito.`when`(jwtTokenProvider.parseClaims(token)).thenReturn(tokenBody)

        Mockito.doNothing()
            .`when`(jwtTokenProvider)
            .validate(token)

        jwtAuthenticationFilter!!.doFilterInternal(request!!, response!!, filterChain!!)

        Assertions.assertThat(response!!.contentAsString).isEmpty()
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(request, response)
    }

    @Test
    @DisplayName("token이 null이면 ErorrReponse 를 반환한다.")
    @Throws(Exception::class)
    fun token_is_null_exception() {
        request!!.requestURI = "/vacations"

        request!!.addHeader("Authorization", "Bearer ")

        Mockito.`when`(jwtTokenProvider!!.extractToken(request!!)).thenReturn(null)

        jwtAuthenticationFilter!!.doFilterInternal(request!!, response!!, filterChain!!)

        org.junit.jupiter.api.Assertions.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response!!.status)

        val errorResponse = objectMapper!!.readValue(
            response!!.contentAsString,
            ErrorResponse::class.java
        )

        org.junit.jupiter.api.Assertions.assertEquals("UNAUTHORIZED_INVALID_HEADER", errorResponse.codeName)
    }

    @Test
    @DisplayName("token이 validate를 통과하지 못하면 errorResponse를 반환한다.")
    @Throws(
        Exception::class
    )
    fun token_is_invalid() {
        val token = "token"

        request!!.requestURI = "/vacations"

        request!!.addHeader("Authorization", "Bearer $token")

        Mockito.`when`(jwtTokenProvider!!.extractToken(request!!)).thenReturn(token)

        Mockito.doThrow(UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_INVALID_TOKEN))
            .`when`(jwtTokenProvider)
            .validate(token)

        jwtAuthenticationFilter!!.doFilterInternal(request!!, response!!, filterChain!!)

        org.junit.jupiter.api.Assertions.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response!!.status)

        val errorResponse = objectMapper!!.readValue(
            response!!.contentAsString,
            ErrorResponse::class.java
        )
        org.junit.jupiter.api.Assertions.assertEquals("UNAUTHORIZED_INVALID_TOKEN", errorResponse.codeName)
    }
}