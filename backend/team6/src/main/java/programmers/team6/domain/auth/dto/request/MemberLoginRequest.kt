package programmers.team6.domain.auth.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank


data class MemberLoginRequest(

    @field:Email(message = "이메일 형식이 아닙니다.")
    @field:NotBlank(message = "이메일 입력은 필수입니다.")
    val email: String?,

    @field:NotBlank(message = "비밀번호 입력은 필수입니다.")
    val password: String?
)
