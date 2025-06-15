package programmers.team6.domain.admin.controller

import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import programmers.team6.domain.admin.dto.request.CodeCreateRequest
import programmers.team6.domain.admin.dto.response.AdminCodeResponse
import programmers.team6.domain.admin.service.CodeService

@RestController
@RequestMapping("/admin/code")
class CodeController(
    private val codeService: CodeService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun registerCode(
        @RequestBody
        @Valid
        codeCreateRequest: CodeCreateRequest
    ) {
        codeService.createCode(codeCreateRequest)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun retrieveCodePage(
        @PageableDefault(page = 0, size = 20) pageable: Pageable,
        @RequestParam(name = "groupCode", required = false) groupCode: String?
    ): AdminCodeResponse {
        return codeService.readCodePage(pageable, groupCode)
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun modifyCode(
        @PathVariable("id")
        id: Long,
        @RequestBody
        codeCreateRequest:
        @Valid
        CodeCreateRequest
    ) {
        codeService.updateCode(id, codeCreateRequest)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCode(@PathVariable("id") id: Long) {
        codeService.deleteCode(id)
    }
}
