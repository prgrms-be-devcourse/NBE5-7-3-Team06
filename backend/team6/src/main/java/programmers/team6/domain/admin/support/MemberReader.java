package programmers.team6.domain.admin.support;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.VacationStatisticsRequest;
import programmers.team6.domain.admin.repository.VacationInfoLogSearchRepository;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.repository.MemberSearchRepository;

@Component
@RequiredArgsConstructor
public class MemberReader {

	private final MemberSearchRepository memberRepository;
	private final VacationInfoLogSearchRepository vacationInfoLogSearchRepository;

	public Members readHasVacationInfoMemberFrom(VacationStatisticsRequest request, Pageable pageable) {
		LocalDateTime date = LocalDateTime.of(request.year(), 12, 31, 23, 59);

		List<Long> ids = vacationInfoLogSearchRepository.queryContainVacationInfoMemberIds(date,
			request.vacationCode());
		Page<Member> members = memberRepository.searchFrom(request.deptId(), request.name(), ids, pageable);
		return new Members(members);
	}
}
