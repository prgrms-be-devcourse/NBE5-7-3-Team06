package programmers.team6.domain.vacation.support;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.response.ApprovalStepDetailUpdateResponse;
import programmers.team6.domain.admin.dto.response.VacationRequestDetailReadResponse;
import programmers.team6.domain.vacation.repository.ApprovalStepRepository;
import programmers.team6.domain.vacation.repository.VacationRequestRepository;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.customException.NotFoundException;

@Component
@RequiredArgsConstructor
public class VacationRequestReader {

	private final VacationRequestRepository vacationRequestRepository;
	private final ApprovalStepRepository approvalStepRepository;

	public VacationRequestDetailReadResponse readDetailFrom(Long id) {
		return vacationRequestRepository.findVacationRequestDetailById(id)
			// .orElseThrow(() -> new NotFoundException(NotFoundErrorCode.NOT_FOUND_VACATION_REQUEST))
			.injectApprovalStepDetails(readApprovalFrom(id));
	}

	private List<ApprovalStepDetailUpdateResponse> readApprovalFrom(Long id) {
		List<ApprovalStepDetailUpdateResponse> approvalStepDetailUpdateResponses =
			approvalStepRepository.findApprovalStepDetailById(id);
		if (approvalStepDetailUpdateResponses.isEmpty()) {
			throw new NotFoundException(NotFoundErrorCode.NOT_FOUND_APPROVAL_STEP);
		}
		return approvalStepDetailUpdateResponses;
	}
}
