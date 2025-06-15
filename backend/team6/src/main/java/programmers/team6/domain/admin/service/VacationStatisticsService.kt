package programmers.team6.domain.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest;
import programmers.team6.domain.admin.support.MemberReader;
import programmers.team6.domain.admin.support.Members;
import programmers.team6.domain.admin.support.VacationInfoLogReader;
import programmers.team6.domain.admin.support.VacationInfoLogs;
import programmers.team6.domain.admin.support.VacationRequests;
import programmers.team6.domain.admin.support.VacationRequestsReader;
import programmers.team6.domain.admin.utils.mapper.VacationStatisticsMapper;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.vacation.dto.response.VacationMonthlyStatisticsResponse;

@Service
@RequiredArgsConstructor
public class VacationStatisticsService {

	private final MemberRepository memberRepository;
	private final VacationInfoLogReader vacationInfoLogReader;
	private final VacationRequestsReader vacationRequestsReader;
	private final MemberReader memberReader;
	private final VacationStatisticsMapper mapper;

	@Transactional(readOnly = true)
	public Page<VacationMonthlyStatisticsResponse> getMonthlyVacationStatistics(VacationStatisticsRequest request,
		Pageable pageable) {
		Members members = memberReader.readHasVacationInfoMemberFrom(request, pageable);
		VacationRequests vacationRequests = vacationRequestsReader.vacationRequestFrom(members.toIds(), request);
		VacationInfoLogs logs = vacationInfoLogReader.lastedLogsFrom(members.toIds(), request);
		return mapper.toDto(members, vacationRequests, logs, request.year());
	}
}
