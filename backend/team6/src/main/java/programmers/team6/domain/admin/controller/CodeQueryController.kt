package programmers.team6.domain.admin.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import programmers.team6.domain.admin.dto.response.CodeDropdownResponse
import programmers.team6.domain.admin.service.CodeService

@RestController
@RequestMapping("/codes")
class CodeQueryController(
    private val codeService: CodeService
) {

    @GetMapping("/group/{groupCode}")
    @ResponseStatus(HttpStatus.OK)
    fun getCodesByGroupCode(@PathVariable groupCode: String): MutableList<CodeDropdownResponse> {
        return codeService.getCodesByGroupCode(groupCode)
    }
}
