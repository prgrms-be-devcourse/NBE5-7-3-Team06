package programmers.team6.domain.admin.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import programmers.team6.domain.admin.dto.response.MemberApprovalResponse
import programmers.team6.domain.admin.service.MemberApprovalService

@RestController
@RequestMapping("/admin/member-approvals")
class MemberApprovalController(
    private val memberApprovalService: MemberApprovalService
) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun pendingMembers(): List<MemberApprovalResponse> {
        return memberApprovalService.findPendingMembers()
    }

    @PostMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun approveMember(@PathVariable memberId: Long) {
        memberApprovalService.approveMember(memberId)
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMember(@PathVariable memberId: Long) {
        memberApprovalService.deleteMember(memberId)
    }
}
