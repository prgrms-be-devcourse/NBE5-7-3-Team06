package programmers.team6.domain.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.VacationRequestDetailReadResponse;
import programmers.team6.domain.auth.dto.TokenBody;
import programmers.team6.domain.member.service.MemberVacationRequestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members/vacation-request")
public class MemberVacationRequestController {

	private final MemberVacationRequestService memberVacationRequestService;

	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	VacationRequestDetailReadResponse showVacationRequestDetail(@PathVariable Long id,
		@AuthenticationPrincipal TokenBody tokenBody) {
		Long memberId = tokenBody.id();
		return memberVacationRequestService.selectVacationRequestDetailById(id, memberId);
	}
}
