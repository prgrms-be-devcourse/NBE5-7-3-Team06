package programmers.team6.domain.vacation.rule;

import java.time.LocalDate;
import java.util.List;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.enums.VacationCode;

public interface VacationGrantRule {
	boolean canUpdate(double totalCount);

	VacationInfo createVacationInfo(Long memberId);

	boolean isSameType(VacationCode vacationCode);

	List<LocalDate> getBaseLineDates(LocalDate date);

	String getTypeCode();

	VacationInfoLog grant(LocalDate date, Member member, VacationInfo info);
}
