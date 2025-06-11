package programmers.team6.domain.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.member.annotation.LoginMember;
import programmers.team6.domain.member.dto.MemberLoginInfoResponse;
import programmers.team6.domain.member.service.MemberService;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@GetMapping("/me")
	@ResponseStatus(HttpStatus.OK)
	public MemberLoginInfoResponse findLoginMemberInfo(@LoginMember MemberLoginInfoResponse memberInfo) {

		return memberInfo;
	}

}
