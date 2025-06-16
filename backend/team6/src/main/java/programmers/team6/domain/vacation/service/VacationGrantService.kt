package programmers.team6.domain.vacation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.enums.VacationCode;
import programmers.team6.domain.vacation.repository.VacationInfoRepository;
import programmers.team6.domain.vacation.rule.VacationGrantRule;
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder;
import programmers.team6.domain.vacation.rule.VacationGrantRules;
import programmers.team6.domain.vacation.support.VacationInfos;
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher;

@Service
@RequiredArgsConstructor
public class VacationGrantService {
	private final MemberRepository memberRepository;
	private final VacationInfoRepository vacationInfoRepository;
	private final VacationGrantRuleFinder vacationGrantRuleFinder;
	private final VacationInfoLogPublisher vacationInfoLogPublisher;

	@Transactional
	public void grantAnnualVacations(LocalDate date) {
		VacationGrantRules rules = vacationGrantRuleFinder.findAll();
		VacationInfos vacationInfos = selectGrantVacationInfos(date, rules);
		grantVacations(date, rules, vacationInfos);
	}

	private VacationInfos selectGrantVacationInfos(LocalDate date, VacationGrantRules rules) {
		List<VacationInfo> result = new ArrayList<>();
		for (VacationGrantRule rule : rules.getRules()) {
			List<LocalDate> baseLineDates = rule.getBaseLineDates(date);
			result.addAll(selectVacationInfo(rule, baseLineDates));
		}
		return new VacationInfos(result);
	}

	private List<VacationInfo> selectVacationInfo(VacationGrantRule rule, List<LocalDate> baseLineDates) {
		if (rule.isSameType(VacationCode.ANNUAL)) {
			return vacationInfoRepository.findAnnualVacationByJoinDates(VacationCode.ANNUAL.getCode(), baseLineDates);
		}
		return vacationInfoRepository.findByTypeAndCreatedAtToDate(rule.getTypeCode(), baseLineDates);
	}

	private void grantVacations(LocalDate date, VacationGrantRules reules, VacationInfos infos) {
		for (Long id : infos.getMemberIds()) {
			List<VacationInfo> memberInfos = infos.getByMemberId(id);
			Member member = memberRepository.findById(id).orElseThrow(() -> new RuntimeException());
			List<VacationInfoLog> logs = reules.grant(date, member, new VacationInfos(memberInfos));
			vacationInfoLogPublisher.publish(logs);
		}
	}
}
