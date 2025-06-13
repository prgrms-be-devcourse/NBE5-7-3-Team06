package programmers.team6.domain.vacation.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import programmers.team6.domain.auth.dto.TokenBody;
import programmers.team6.domain.vacation.dto.response.ApprovalFirstStepDetailResponse;
import programmers.team6.domain.vacation.dto.response.ApprovalFirstStepSelectResponse;
import programmers.team6.domain.vacation.dto.response.ApprovalSecondStepDetailResponse;
import programmers.team6.domain.vacation.dto.response.ApprovalSecondStepSelectResponse;
import programmers.team6.domain.vacation.dto.request.ApprovalStepRejectRequest;
import programmers.team6.domain.vacation.dto.request.ApprovalStepSelectRequest;
import programmers.team6.domain.vacation.service.ApprovalStepService;
import programmers.team6.global.paging.PagingConfig;

@RestController
@RequestMapping("/approval-steps")
@RequiredArgsConstructor
public class ApprovalStepController {

	private final ApprovalStepService approvalStepService;

	@GetMapping("/first")
	@ResponseStatus(HttpStatus.OK)
	public Page<ApprovalFirstStepSelectResponse> getFirstStep(
		@AuthenticationPrincipal TokenBody tokenBody,
		ApprovalStepSelectRequest request, @PagingConfig Pageable pageable) {

		if (!request.hasFilter()) {
			return approvalStepService.findFirstStepByMemberId(tokenBody.id(), pageable);
		} else {
			return approvalStepService.findFirstStepByFilter(request, tokenBody.id(), pageable);
		}
	}

	@GetMapping("/first/{approvalStepId}")
	@ResponseStatus(HttpStatus.OK)
	public ApprovalFirstStepDetailResponse getFirstStepDetail(@AuthenticationPrincipal TokenBody tokenBody,
		@PathVariable Long approvalStepId) {

		return approvalStepService.findFirstStepDetailById(approvalStepId, tokenBody.id());
	}

	@GetMapping("/second")
	@ResponseStatus(HttpStatus.OK)
	public Page<ApprovalSecondStepSelectResponse> getSecondStep(
		@AuthenticationPrincipal TokenBody tokenBody,
		ApprovalStepSelectRequest request, @PagingConfig Pageable pageable) {

		if (!request.hasFilter()) {
			return approvalStepService.findSecondStepByMemberId(tokenBody.id(), pageable);
		} else {
			return approvalStepService.findSecondStepByFilter(request, tokenBody.id(), pageable);
		}
	}

	@GetMapping("/second/{approvalStepId}")
	@ResponseStatus(HttpStatus.OK)
	public ApprovalSecondStepDetailResponse getSecondStepDetail(@AuthenticationPrincipal TokenBody tokenBody,
		@PathVariable Long approvalStepId) {

		return approvalStepService.findSecondStepDetailById(approvalStepId, tokenBody.id());
	}

	@PatchMapping("/first/{approvalStepId}/approve")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void approveFirstStep(@AuthenticationPrincipal TokenBody tokenBody, @PathVariable Long approvalStepId) {

		approvalStepService.approveFirstStep(approvalStepId, tokenBody.id());
	}

	@PatchMapping("/first/{approvalStepId}/reject")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void rejectFirstStep(@AuthenticationPrincipal TokenBody tokenBody, @PathVariable Long approvalStepId,
		@Valid @RequestBody ApprovalStepRejectRequest request) {
		approvalStepService.rejectFirstStep(approvalStepId, tokenBody.id(), request);
	}

	@PatchMapping("/second/{approvalStepId}/approve")
	@ResponseStatus(HttpStatus.OK)
	public boolean approveSecondStep(@AuthenticationPrincipal TokenBody tokenBody, @PathVariable Long approvalStepId) {
		return approvalStepService.approveSecondStep(approvalStepId, tokenBody.id());
	}

	@PatchMapping("/second/{approvalStepId}/reject")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void rejectSecondStep(@AuthenticationPrincipal TokenBody tokenBody, @PathVariable Long approvalStepId,
		@Valid @RequestBody ApprovalStepRejectRequest request) {

		approvalStepService.rejectSecondStep(approvalStepId, tokenBody.id(), request);
	}

}
