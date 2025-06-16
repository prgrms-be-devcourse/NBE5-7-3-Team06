package programmers.team6.domain.auth.controller

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import programmers.team6.domain.auth.dto.request.MemberLoginRequest
import programmers.team6.domain.auth.dto.request.MemberSignUpRequest
import programmers.team6.domain.auth.dto.response.AccessTokenResponse
import programmers.team6.domain.auth.dto.response.AuthTokenResponse
import programmers.team6.domain.auth.service.AuthService
import programmers.team6.domain.auth.util.JwtUtils

@RestController
@RequestMapping("/auth")
class AuthController (
    private val authService: AuthService
) {


    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp( @Valid @RequestBody memberSignUpRequest: MemberSignUpRequest) {
        authService.signUp(memberSignUpRequest)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/email-duplicate-check")
    fun isEmailDuplicated(@RequestParam email: String): Map<String, Boolean> {
        val isEmailDuplicated = authService.isExistsByEmail(email)

        return mapOf("isEmailDuplicated" to  isEmailDuplicated)
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(
        @RequestBody memberLoginRequest: MemberLoginRequest,
        response: HttpServletResponse
    ): Map<String, AuthTokenResponse> {
        val loginResponse = authService.login(memberLoginRequest)

        val refreshToken = loginResponse.refreshToken

        JwtUtils.addRefreshTokenCookie(response, refreshToken, loginResponse.refreshTokenExpiresIn)

        return mapOf("token" to loginResponse.authTokenResponse)
    }

    @PostMapping("/reissue")
    @ResponseStatus(HttpStatus.OK)
    fun refresh(
        @CookieValue("refreshToken") refreshToken: String
    ): AccessTokenResponse {
        val accessToken = authService.reissue(refreshToken)

        return accessToken
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    fun logout(@CookieValue("refreshToken") refreshToken: String): ResponseEntity<Void> {

        authService.addBlackList(refreshToken)

        val deleteCookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .maxAge(0)
            .build()

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
            .build()
    }
}