package programmers.team6.domain.auth.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.admin.repository.DeptRepository
import programmers.team6.domain.auth.dto.JwtMemberInfo
import programmers.team6.domain.auth.dto.TokenBody
import programmers.team6.domain.auth.dto.TokenPairWithExpiration
import programmers.team6.domain.auth.dto.request.MemberLoginRequest
import programmers.team6.domain.auth.dto.request.MemberSignUpRequest
import programmers.team6.domain.auth.dto.response.AccessTokenResponse
import programmers.team6.domain.auth.dto.response.AuthTokenResponse
import programmers.team6.domain.auth.token.JwtTokenProvider
import programmers.team6.domain.auth.util.JwtUtils
import programmers.team6.domain.auth.util.JwtUtils.calculateTtlMillis
import programmers.team6.domain.member.enums.Role
import programmers.team6.domain.member.repository.MemberInfoRepository
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.global.exception.code.ConflictErrorCode
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.code.UnauthorizedErrorCode
import programmers.team6.global.exception.customException.ConflictException
import programmers.team6.global.exception.customException.NotFoundException
import programmers.team6.global.exception.customException.UnauthorizedException
import programmers.team6.support.MemberMother
import programmers.team6.support.PositionMother
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*


internal class AuthServiceUnitTests_kt {
    private val memberRepository: MemberRepository = mockk<MemberRepository>()

    private val memberInfoRepository: MemberInfoRepository = mockk<MemberInfoRepository>()

    private val passwordEncoder: PasswordEncoder = mockk<PasswordEncoder>()

    private val deptRepository: DeptRepository = mockk<DeptRepository>()

    private val codeRepository: CodeRepository = mockk<CodeRepository>()

    private val jwtTokenProvider: JwtTokenProvider = mockk<JwtTokenProvider>()

    private val jwtService: JwtService = mockk<JwtService>()

    private val authService: AuthService = AuthService(
        memberRepository,
        memberInfoRepository,
        deptRepository,
        codeRepository,
        passwordEncoder,
        jwtTokenProvider,
        jwtService
    )

    @Test
    @DisplayName("회원가입 성공 테스트")
    fun signUp_success() {
        val encodedPassword = "encoded1234"

        val dept = Dept.builder()
            .deptName("개발팀")
            .build()

        val position = PositionMother.employee()

        val memberReq = genMemberSignUpRequest()

        every{
            deptRepository.findByIdOrNull(memberReq.dept)
        } returns dept

        every {
            codeRepository.findByGroupCodeAndCode(
                "POSITION",
                memberReq.position!!
            )
        } returns position

       every {
           memberInfoRepository.existsByEmail(memberReq.email)
       } returns false

       every {
           passwordEncoder.encode(memberReq.password)
       } returns encodedPassword


        authService.signUp(memberReq)

        val captor = ArgumentCaptor.forClass(
            Member::class.java
        )

        Mockito.verify(memberRepository).save(captor.capture())

        val saved = captor.value

        assertThat(saved)
            .extracting("name", "joinDate", "role")
            .containsExactly(memberReq.name, memberReq.joinDate, Role.PENDING)

        assertThat(saved.memberInfo)
            .extracting("email", "password", "birth")
            .containsExactly(memberReq.email, encodedPassword, memberReq.birth)

        assertThat(saved.dept.deptName).isEqualTo(dept.deptName)

        assertThat(saved.position.code).isEqualTo(memberReq.position)
    }

    @Test
    @DisplayName("회원가입 시 없는 부서정보가 들어오면 예외를 반환한다.")
    fun signUp_dept_exception() {
        val memberReq = genMemberSignUpRequest()

        Mockito.`when`(deptRepository!!.findById(memberReq.dept)).thenReturn(Optional.empty())

        assertThatThrownBy { authService!!.signUp(memberReq) }.isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", NotFoundErrorCode.NOT_FOUND_DEPT)
    }

    @Test
    @DisplayName("회원가입 시 없는 직위코드가 들어오면 예외를 반환한다.")
    fun signUp_position_exception() {
        val memberReq = genMemberSignUpRequest()

        val dept = Dept.builder()
            .deptName("개발팀")
            .build()

        Mockito.`when`(deptRepository!!.findById(memberReq.dept)).thenReturn(Optional.of(dept))
        Mockito.`when`<Code?>(
            codeRepository!!.findByGroupCodeAndCode(
                "POSITION",
                memberReq.position!!
            )
        ).thenReturn(Optional.empty<T>())

        assertThatThrownBy { authService!!.signUp(memberReq) }.isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", NotFoundErrorCode.NOT_FOUND_POSITION)
    }

    @Test
    @DisplayName("회원가입 시 이메일 중복이면 예외를 반환한다.")
    fun signUp_email_exception() {
        val memberReq = genMemberSignUpRequest()

        val dept = Dept.builder()
            .deptName("개발팀")
            .build()

        val position = PositionMother.employee()

        Mockito.`when`(deptRepository!!.findById(memberReq.dept)).thenReturn(Optional.of(dept))
        Mockito.`when`<Code?>(
            codeRepository!!.findByGroupCodeAndCode(
                "POSITION",
                memberReq.position!!
            )
        ).thenReturn(Optional.of(position))
        Mockito.`when`(memberInfoRepository!!.existsByEmail(memberReq.email)).thenReturn(true)

        assertThatThrownBy { authService!!.signUp(memberReq) }.isInstanceOf(ConflictException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ConflictErrorCode.CONFLICT_EMAIL)
    }

    @Test
    @DisplayName("로그인 성공")
    fun login_successfully() {
        val member = MemberMother.withId(1L)
        val info = member.memberInfo
        val email = info.email
        val password = info.password

        Mockito.`when`<Member?>(memberRepository!!.findByEmail(email)).thenReturn(Optional.of(member))

        Mockito.`when`(passwordEncoder!!.matches(password, password)).thenReturn(true)

        val tokenPair = TokenPairWithExpiration("accessToken", "refreshToken", 200, 1000)
        Mockito.`when`(jwtTokenProvider!!.generateTokenPair(JwtMemberInfo(1L, member.name, member.role))).thenReturn(
            tokenPair
        )

        val response = authService!!.login(MemberLoginRequest(email, password))

        val authTokenResponse = AuthTokenResponse(
            tokenPair.accessToken,
            tokenPair.accessTokenExpiresIn, member.id,
            member.name, member.role
        )
        assertThat(response.authTokenResponse).isEqualTo(authTokenResponse)
        assertThat(response.refreshToken).isEqualTo(tokenPair.refreshToken)
        assertThat(response.refreshTokenExpiresIn).isEqualTo(tokenPair.refreshTokenExpiresIn)
    }

    @Test
    @DisplayName("이메일이 존재하지않는 경우 실패한다")
    fun fails_login_when_email_is_not_found() {
        val member = MemberMother.withId(1L)
        val info = member.memberInfo
        val email = info.email
        val password = info.password

        Mockito.`when`<Member?>(memberRepository!!.findByEmail(email)).thenReturn(Optional.empty<T>())
        val memberLoginRequest = MemberLoginRequest(email, password)

        assertThatThrownBy { authService!!.login(memberLoginRequest) }
            .isInstanceOf(NotFoundException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", NotFoundErrorCode.NOT_FOUND_EMAIL)
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 테스트가 실패한다")
    fun fails_login_when_password_does_not_match() {
        val member = MemberMother.withId(1L)
        val info = member.memberInfo
        val email = info.email
        val password = info.password
        val inputPassword = "invalidpassword"
        Mockito.`when`<Member?>(memberRepository!!.findByEmail(email)).thenReturn(Optional.of(member))

        Mockito.`when`(passwordEncoder!!.matches(inputPassword, password)).thenReturn(false)
        val memberLoginRequest = MemberLoginRequest(email, inputPassword)

        assertThatThrownBy { authService!!.login(memberLoginRequest) }
            .isInstanceOf(UnauthorizedException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", UnauthorizedErrorCode.UNAUTHORIZED_PASSWORD)
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 액세스 토큰을 재발급한다")
    fun reissuesAccessToken_whenRefreshTokenIsValid() {
        val refreshToken = UUID.randomUUID().toString()
        val accessToken = UUID.randomUUID().toString()
        val authTokenResponse = AccessTokenResponse(accessToken, 100)
        Mockito.`when`(jwtTokenProvider!!.generateAccessToken(refreshToken)).thenReturn(authTokenResponse)

        val reissue = authService!!.reissue(refreshToken)

        assertThat(reissue.accessToken).isEqualTo(accessToken)
    }

    @Test
    @DisplayName("리프레시 토큰의 만료 시간 기반으로 블랙리스트를 등록한다")
    fun addsRefreshTokenToBlacklist_basedOnExpirationTime() {
        val refreshToken = UUID.randomUUID().toString()
        val expiration = LocalDateTime.of(2025, 6, 13, 0, 1, 0)
        val date = Date.from(
            expiration.toInstant(
                ZoneOffset.UTC
            )
        )
        Mockito.`when`(jwtTokenProvider!!.parseClaims(refreshToken))
            .thenReturn(TokenBody(1L, "name", Role.USER, date, date))
        Mockito.mockStatic<JwtUtils>(JwtUtils::class.java).use { jwtUtilsMockedStatic ->
            jwtUtilsMockedStatic.`when`<Any> { calculateTtlMillis(date) }.thenReturn(0L)
            authService!!.addBlackList(refreshToken)
            Mockito.verify(jwtService, Mockito.times(1)).addBlackList(refreshToken, 0L)
        }
    }

    companion object {
        private fun genMemberSignUpRequest(): MemberSignUpRequest {
            return MemberSignUpRequest(
                "member1",
                "test@test.com",
                1L,
                "01",
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "1989-10-10",
                "qwer1234!"
            )
        }
    }
}
