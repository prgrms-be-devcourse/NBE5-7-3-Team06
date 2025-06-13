package programmers.team6.domain.admin.utils.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import programmers.team6.domain.admin.service.Members;
import programmers.team6.domain.admin.service.TargetVacationRequests;
import programmers.team6.domain.admin.service.VacationInfoLogs;
import programmers.team6.domain.admin.service.VacationRequests;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.dto.VacationMonthlyStatisticsResponse;
import programmers.team6.domain.vacation.entity.VacationInfoLog;

@Component
public class VacationStatisticsMapper {
	public Page<VacationMonthlyStatisticsResponse> toDto(Members members, VacationRequests vacationRequests,
		VacationInfoLogs logs, Integer year) {
		return members.toPages().map(member -> toDto(member, vacationRequests, logs, year));
	}

	private VacationMonthlyStatisticsResponse toDto(Member member, VacationRequests vacationRequests,
		VacationInfoLogs logs, Integer year) {
		TargetVacationRequests targeted = vacationRequests.targetRequests(member.getId());
		VacationInfoLog vacationInfo = logs.findVacationInfo(member.getId());
		return new VacationMonthlyStatisticsResponse(member.getId(),
			member.getName(),
			vacationInfo.getTotalCount(),
			vacationInfo.getUseCount(),
			vacationInfo.remainingCount(),
			targeted.count(year, 1),
			targeted.count(year, 2),
			targeted.count(year, 3),
			targeted.count(year, 4),
			targeted.count(year, 5),
			targeted.count(year, 6),
			targeted.count(year, 7),
			targeted.count(year, 8),
			targeted.count(year, 9),
			targeted.count(year, 10),
			targeted.count(year, 11),
			targeted.count(year, 12)
		);
	}
}
