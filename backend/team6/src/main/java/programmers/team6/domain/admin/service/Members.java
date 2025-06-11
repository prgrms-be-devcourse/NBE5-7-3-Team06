package programmers.team6.domain.admin.service;

import java.util.List;

import org.springframework.data.domain.Page;

import programmers.team6.domain.member.entity.Member;

public class Members {
	private final Page<Member> members;

	public Members(Page<Member> members) {
		this.members = members;
	}

	public List<Long> toIds() {
		return members.stream().map(Member::getId).toList();
	}

	public Page<Member> toPages() {
		return members;
	}
}
