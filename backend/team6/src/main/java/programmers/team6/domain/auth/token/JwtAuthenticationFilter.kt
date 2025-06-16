package programmers.team6.domain.auth.token

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import programmers.team6.domain.auth.dto.TokenBody
import programmers.team6.global.exception.code.UnauthorizedErrorCode
import programmers.team6.global.exception.customException.UnauthorizedException
import programmers.team6.global.util.ErrorResponseUtil.setErrorResponse
import java.io.IOException

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
) : OncePerRequestFilter() {

    companion object {
        private val TOKEN_FREE_URIS = listOf(
            "/auth", "/codes", "/depts"
        )
    }

    @Throws(ServletException::class, IOException::class)
    public override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val uri: String = request.requestURI

        val tokenFree: Boolean = TOKEN_FREE_URIS.any { uri.startsWith(it) }

        if (tokenFree) {
            filterChain.doFilter(request, response)
            return
        }

        val token: String? = jwtTokenProvider.extractToken(request)

        if (token == null) {
            setErrorResponse(response, UnauthorizedErrorCode.UNAUTHORIZED_INVALID_HEADER)
            return
        }

        try {
            jwtTokenProvider.validate(token)
        } catch (e: UnauthorizedException) {
            setErrorResponse(response, e.errorCode)
            return
        }

        val tokenBody: TokenBody = jwtTokenProvider.parseClaims(token)

        val auth: Authentication = UsernamePasswordAuthenticationToken(
            tokenBody, null, listOf(SimpleGrantedAuthority(tokenBody.role.toString()))
        )

        SecurityContextHolder.getContext().authentication = auth

        filterChain.doFilter(request, response)
    }


}
