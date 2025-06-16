package programmers.team6.domain.member.controller

import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import programmers.team6.domain.admin.dto.response.VacationRequestDetailReadResponse
import programmers.team6.domain.auth.dto.TokenBody
import programmers.team6.domain.member.service.MemberVacationRequestService

@RestController
@RequestMapping("/members/vacation-request")
class MemberVacationRequestController(private val memberVacationRequestService: MemberVacationRequestService) {

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun showVacationRequestDetail(
        @PathVariable id: Long,
        @AuthenticationPrincipal tokenBody: TokenBody
    ): VacationRequestDetailReadResponse {
        val memberId = tokenBody.id
        return memberVacationRequestService.selectVacationRequestDetailById(id, memberId)
    }
}
