package programmers.team6.domain.admin.dto.response

import org.springframework.data.domain.Page

data class AdminCodeResponse(
    val codeReadResponse: Page<CodeReadResponse>,
    val groupCodes: List<String>
)
