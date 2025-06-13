package programmers.team6.domain.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.MemberApprovalResponse;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.enums.Role;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.member.service.MemberService;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.enums.VacationCode;
import programmers.team6.domain.vacation.repository.VacationInfoRepository;
import programmers.team6.domain.vacation.rule.VacationGrantRule;
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder;
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberApprovalService {

	private final MemberRepository memberRepository;
	private final VacationGrantRuleFinder vacationGrantRuleFinder;
	private final VacationInfoRepository vacationInfoRepository;
	private final MemberService memberService;
	private final VacationInfoLogPublisher vacationInfoLogPublisher;

	public List<MemberApprovalResponse> findPendingMembers() {
		return memberRepository.findPendingMembers(Role.PENDING);
	}

	@Transactional
	public void approveMember(Long memberId) {
		Member member = memberService.findById(memberId);
		member.approve();

		initInfo(member);
	}

	private void initInfo(Member member) {
		//TODO : 추후 batch insert를 고민해봐야 할듯
		for (VacationCode type : VacationCode.values()) {
			VacationGrantRule vacationRule = vacationGrantRuleFinder.find(type);
			VacationInfo vacationInfo = vacationRule.createVacationInfo(member.getId());
			vacationInfoRepository.save(vacationInfo);
			vacationInfoLogPublisher.publish(vacationInfo.toLog());
		}
	}

	@Transactional
	public void deleteMember(Long memberId) {
		Member member = memberService.findById(memberId);
		member.validateDeletableOnReject();
		memberRepository.delete(member);
	}
}
