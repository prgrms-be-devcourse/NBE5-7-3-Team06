package programmers.team6.domain.admin.dto.request

import jakarta.validation.constraints.NotBlank

data class CodeCreateRequest(
    @NotBlank
    val groupCode: String,
    @NotBlank
    val code: String,
    @NotBlank
    val name: String
)
