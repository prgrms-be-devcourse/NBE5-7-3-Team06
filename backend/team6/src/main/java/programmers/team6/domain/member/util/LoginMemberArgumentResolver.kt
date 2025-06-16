package programmers.team6.domain.member.util

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import programmers.team6.domain.auth.token.JwtTokenProvider
import programmers.team6.domain.member.annotation.LoginMember
import programmers.team6.domain.member.dto.response.MemberLoginInfoResponse
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.code.UnauthorizedErrorCode
import programmers.team6.global.exception.customException.NotFoundException
import programmers.team6.global.exception.customException.UnauthorizedException

@Component
class LoginMemberArgumentResolver(
    private val jwtTokenProvider: JwtTokenProvider,
    private val memberRepository: MemberRepository
) : HandlerMethodArgumentResolver {


    override fun supportsParameter(parameter: MethodParameter): Boolean {
       return parameter.hasParameterAnnotation(LoginMember::class.java)
                && parameter.parameterType == MemberLoginInfoResponse::class.java
    }

    @Throws(Exception::class)
    override fun resolveArgument(
        parameter: MethodParameter, mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest, binderFactory: WebDataBinderFactory?
    ): MemberLoginInfoResponse {
        val nativeRequest = webRequest.nativeRequest as HttpServletRequest

        val accessToken = jwtTokenProvider.extractToken(nativeRequest)
            ?: throw UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_INVALID_HEADER)

        val tokenBody = jwtTokenProvider.parseClaims(accessToken)

        val id = tokenBody.id

        val loginMemberInfo = memberRepository.findLoginMemberInfo(id)
            ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_MEMBER)

        return loginMemberInfo
    }
}
