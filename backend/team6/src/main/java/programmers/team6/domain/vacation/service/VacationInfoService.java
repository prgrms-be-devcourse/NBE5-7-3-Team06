package programmers.team6.domain.vacation.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.VacationInfoUpdateTotalCountRequest;
import programmers.team6.domain.admin.dto.VacationInfoUpdateTotalCountRequests;
import programmers.team6.domain.admin.dto.VacationInfoUpdateTotalCountRequestsList;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.repository.VacationInfoRepository;
import programmers.team6.domain.vacation.rule.VacationGrantRule;
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder;
import programmers.team6.domain.vacation.rule.VacationInfos;
import programmers.team6.global.exception.code.BadRequestErrorCode;
import programmers.team6.global.exception.code.ConflictErrorCode;
import programmers.team6.global.exception.customException.BadRequestException;
import programmers.team6.global.exception.customException.ConflictException;

@RequiredArgsConstructor
@Service
public class VacationInfoService {

	private final VacationInfoRepository vacationInfoRepository;
	private final VacationGrantRuleFinder vacationGrantRuleFinder;
	private final VacationInfoLogPublisher vacationInfoLogPublisher;

	@Transactional
	public void updateFrom(VacationInfoUpdateTotalCountRequestsList request) {
		VacationInfos vacationInfos = findVacationInfos(request.vacationIds());
		updateVacationInfos(request, vacationInfos);
	}

	private VacationInfos findVacationInfos(List<Integer> ids) {
		List<VacationInfo> vacationInfos = vacationInfoRepository.findAllByVacationIdIn(ids);
		return new VacationInfos(vacationInfos);
	}

	private void updateVacationInfos(VacationInfoUpdateTotalCountRequestsList request,
		VacationInfos vacationInfos) {
		for (VacationInfoUpdateTotalCountRequests vacations : request.requests()) {
			updateTotalCount(vacationInfos.getByMemberId(vacations.memberId()), vacations);
		}
	}

	private void updateTotalCount(List<VacationInfo> infos, VacationInfoUpdateTotalCountRequests requests) {
		for (VacationInfo info : infos) {
			Optional<VacationInfoUpdateTotalCountRequest> target = requests.getTarget(info.getVacationType());
			if (target.isEmpty()) {
				continue;
			}
			VacationInfoUpdateTotalCountRequest request = target.get();
			VacationGrantRule vacationGrantRule = vacationGrantRuleFinder.find(info.getVacationType());

			validUpdate(info, vacationGrantRule, request);

			VacationInfoLog log = info.updateTotalCount(request.totalCount());
			vacationInfoLogPublisher.publish(log);
		}
	}

	private void validUpdate(VacationInfo info, VacationGrantRule vacationGrantRule,
		VacationInfoUpdateTotalCountRequest request) {
		if (!vacationGrantRule.canUpdate(request.totalCount())) {
			throw new BadRequestException(BadRequestErrorCode.BAD_REQUEST_INVALID_INPUT);
		}
		if (!info.isSameVersion(request.version())) {
			throw new ConflictException(ConflictErrorCode.CONFLICT_VERSION);
		}
	}
}
