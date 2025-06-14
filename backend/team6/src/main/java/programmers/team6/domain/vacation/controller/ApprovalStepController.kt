package programmers.team6.domain.vacation.controller

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import programmers.team6.domain.auth.dto.TokenBody
import programmers.team6.domain.vacation.dto.request.ApprovalStepRejectRequest
import programmers.team6.domain.vacation.dto.request.ApprovalStepSelectRequest
import programmers.team6.domain.vacation.dto.response.ApprovalFirstStepDetailResponse
import programmers.team6.domain.vacation.dto.response.ApprovalFirstStepSelectResponse
import programmers.team6.domain.vacation.dto.response.ApprovalSecondStepDetailResponse
import programmers.team6.domain.vacation.dto.response.ApprovalSecondStepSelectResponse
import programmers.team6.domain.vacation.service.ApprovalStepService
import programmers.team6.global.paging.PagingConfig

@RestController
@RequestMapping("/approval-steps")
class ApprovalStepController(
    private val approvalStepService: ApprovalStepService
) {
    @GetMapping("/first")
    @ResponseStatus(HttpStatus.OK)
    fun getFirstStep(
        @AuthenticationPrincipal tokenBody: TokenBody,
        request: ApprovalStepSelectRequest,
        @PagingConfig pageable: Pageable
    ): Page<ApprovalFirstStepSelectResponse> {
        return if (!request.hasFilter()) {
            approvalStepService.findFirstStepByMemberId(tokenBody.id, pageable)
        } else {
            approvalStepService.findFirstStepByFilter(request, tokenBody.id, pageable)
        }
    }

    @GetMapping("/first/{approvalStepId}")
    @ResponseStatus(HttpStatus.OK)
    fun getFirstStepDetail(
        @AuthenticationPrincipal tokenBody: TokenBody,
        @PathVariable approvalStepId: Long
    ): ApprovalFirstStepDetailResponse {
        return approvalStepService.findFirstStepDetailById(approvalStepId, tokenBody.id)
    }

    @GetMapping("/second")
    @ResponseStatus(HttpStatus.OK)
    fun getSecondStep(
        @AuthenticationPrincipal tokenBody: TokenBody,
        request: ApprovalStepSelectRequest,
        @PagingConfig pageable: Pageable
    ): Page<ApprovalSecondStepSelectResponse> {
        return if (!request.hasFilter()) {
            approvalStepService.findSecondStepByMemberId(tokenBody.id, pageable)
        } else {
            approvalStepService.findSecondStepByFilter(request, tokenBody.id, pageable)
        }
    }

    @GetMapping("/second/{approvalStepId}")
    @ResponseStatus(HttpStatus.OK)
    fun getSecondStepDetail(
        @AuthenticationPrincipal tokenBody: TokenBody,
        @PathVariable approvalStepId: Long
    ): ApprovalSecondStepDetailResponse {
        return approvalStepService.findSecondStepDetailById(approvalStepId, tokenBody.id)
    }

    @PatchMapping("/first/{approvalStepId}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun approveFirstStep(@AuthenticationPrincipal tokenBody: TokenBody, @PathVariable approvalStepId: Long) {
        approvalStepService.approveFirstStep(approvalStepId, tokenBody.id)
    }

    @PatchMapping("/first/{approvalStepId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun rejectFirstStep(
        @AuthenticationPrincipal tokenBody: TokenBody, @PathVariable approvalStepId: Long,
        @RequestBody @Valid request: ApprovalStepRejectRequest
    ) {
        approvalStepService.rejectFirstStep(approvalStepId, tokenBody.id, request)
    }

    @PatchMapping("/second/{approvalStepId}/approve")
    @ResponseStatus(HttpStatus.OK)
    fun approveSecondStep(@AuthenticationPrincipal tokenBody: TokenBody, @PathVariable approvalStepId: Long): Boolean {
        return approvalStepService.approveSecondStep(approvalStepId, tokenBody.id)
    }

    @PatchMapping("/second/{approvalStepId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun rejectSecondStep(
        @AuthenticationPrincipal tokenBody: TokenBody, @PathVariable approvalStepId: Long,
        @RequestBody @Valid request: ApprovalStepRejectRequest
    ) {
        approvalStepService.rejectSecondStep(approvalStepId, tokenBody.id, request)
    }
}
