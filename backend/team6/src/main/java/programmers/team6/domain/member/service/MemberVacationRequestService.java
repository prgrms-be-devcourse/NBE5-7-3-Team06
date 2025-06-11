package programmers.team6.domain.member.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.VacationRequestDetailReadResponse;
import programmers.team6.domain.vacation.service.VacationRequestReader;
import programmers.team6.global.exception.code.ForbiddenErrorCode;
import programmers.team6.global.exception.customException.ForbiddenException;

@Service
@RequiredArgsConstructor
public class MemberVacationRequestService {

	private final VacationRequestReader vacationRequestReader;

	public VacationRequestDetailReadResponse selectVacationRequestDetailById(Long vacationRequestId, Long memberId) {
		VacationRequestDetailReadResponse details = vacationRequestReader.readDetailFrom(vacationRequestId);
		if (!memberId.equals(details.memberId())) {
			throw new ForbiddenException(ForbiddenErrorCode.FORBIDDEN_NO_AUTHORITY);
		}
		return details;
	}
}
