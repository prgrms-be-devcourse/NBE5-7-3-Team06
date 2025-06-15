package programmers.team6.domain.auth.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.admin.repository.DeptRepository
import programmers.team6.domain.auth.dto.JwtMemberInfo
import programmers.team6.domain.auth.dto.request.MemberLoginRequest
import programmers.team6.domain.auth.dto.request.MemberSignUpRequest
import programmers.team6.domain.auth.dto.response.AccessTokenResponse
import programmers.team6.domain.auth.dto.response.AuthTokenResponse
import programmers.team6.domain.auth.dto.response.LoginResponse
import programmers.team6.domain.auth.token.JwtTokenProvider
import programmers.team6.domain.auth.util.JwtUtils
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.enums.Role
import programmers.team6.domain.member.repository.MemberInfoRepository
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.member.util.mapper.MemberMapper
import programmers.team6.global.exception.code.ConflictErrorCode
import programmers.team6.global.exception.code.ForbiddenErrorCode
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.code.UnauthorizedErrorCode
import programmers.team6.global.exception.customException.ConflictException
import programmers.team6.global.exception.customException.ForbiddenException
import programmers.team6.global.exception.customException.NotFoundException
import programmers.team6.global.exception.customException.UnauthorizedException


@Service
@Transactional
class AuthService(
    private val memberRepository: MemberRepository,
    private val memberInfoRepository: MemberInfoRepository,
    private val deptRepository: DeptRepository,
    private val codeRepository: CodeRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtService: JwtService,
) {

    fun signUp(request: MemberSignUpRequest) {

        val dept:Dept = deptRepository.findByIdOrNull(request.dept)
            ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_DEPT)


        val position:Code = codeRepository.findByGroupCodeAndCode("POSITION", request.position!!)
            ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_POSITION)

        if (isExistsByEmail(request.email)) throw  ConflictException(ConflictErrorCode.CONFLICT_EMAIL)

        val encodedPassword = passwordEncoder.encode(request.password)

        val member = MemberMapper.MemberCreateRequestToEntity(
            request, dept, position, encodedPassword
        )

        memberRepository.save(member)
    }

    fun isExistsByEmail(email: String?): Boolean {
        return memberInfoRepository.existsByEmail(email)
    }

    @Transactional(readOnly = true)
    fun login(memberLoginRequest: MemberLoginRequest): LoginResponse {

        val member:Member = memberRepository.findByEmail(memberLoginRequest.email!!) ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_EMAIL)

        if (member.role == Role.PENDING) throw ForbiddenException(ForbiddenErrorCode.FORBIDDEN_PENDING)


        if(!passwordEncoder.matches(memberLoginRequest.password, member.memberInfo.password)) throw UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_PASSWORD)


        val tokenPair = jwtTokenProvider.generateTokenPair(
            JwtMemberInfo(member.id, member.name, member.role)
        )

        val authTokenResponse = AuthTokenResponse(
            tokenPair.accessToken,
            tokenPair.accessTokenExpiresIn, member.id,
            member.name, member.role
        )

        return LoginResponse(authTokenResponse, tokenPair.refreshToken, tokenPair.refreshTokenExpiresIn)
    }

    fun reissue(refreshToken: String): AccessTokenResponse {
        jwtTokenProvider.validate(refreshToken)

        jwtTokenProvider.validateNotBlackListed(refreshToken)

        return jwtTokenProvider.generateAccessToken(refreshToken)
    }

    fun addBlackList(refreshToken: String) {
        val tokenBody = jwtTokenProvider.parseClaims(refreshToken)
        val expiration = tokenBody.expiration

        jwtService.addBlackList(refreshToken, JwtUtils.calculateTtlMillis(expiration))
    }
}
