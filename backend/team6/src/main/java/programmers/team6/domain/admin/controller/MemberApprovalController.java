package programmers.team6.domain.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.MemberApprovalResponse;
import programmers.team6.domain.admin.service.MemberApprovalService;

@RestController
@RequestMapping("/admin/member-approvals")
@RequiredArgsConstructor
public class MemberApprovalController {

	private final MemberApprovalService memberApprovalService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<MemberApprovalResponse> getPendingMembers() {
		return memberApprovalService.findPendingMembers();
	}

	@PostMapping("/{memberId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void approveMember(@PathVariable Long memberId) {
		memberApprovalService.approveMember(memberId);
	}

	@DeleteMapping("/{memberId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteMember(@PathVariable Long memberId) {
		memberApprovalService.deleteMember(memberId);
	}
}
