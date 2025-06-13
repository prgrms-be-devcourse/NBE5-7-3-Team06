package programmers.team6.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import programmers.team6.domain.admin.dto.ApprovalStepDetailUpdateResponse;
import programmers.team6.domain.admin.dto.VacationRequestDetailReadResponse;
import programmers.team6.domain.vacation.support.VacationRequestReader;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.customException.NotFoundException;

public class VacationRequestReaderFake extends VacationRequestReader {
	private final Map<Long, VacationRequestDetailReadResponse> vacationRequestRepository = new HashMap<>();
	private final Map<Long, List<ApprovalStepDetailUpdateResponse>> approvalStepRepository = new HashMap<>();

	public VacationRequestReaderFake() {
		super(null, null);
	}

	@Override
	public VacationRequestDetailReadResponse readDetailFrom(Long id) {
		if (!vacationRequestRepository.containsKey(id)) {
			throw new NotFoundException(NotFoundErrorCode.NOT_FOUND_VACATION_REQUEST);
		}

		if (!approvalStepRepository.containsKey(id) || approvalStepRepository.get(id).isEmpty()) {
			throw new NotFoundException(NotFoundErrorCode.NOT_FOUND_APPROVAL_STEP);
		}

		return vacationRequestRepository.get(id).injectApprovalStepDetails(approvalStepRepository.get(id));
	}

	public void putVacationRequestDetail(Long key, VacationRequestDetailReadResponse value) {
		vacationRequestRepository.put(key, value);
	}

	public void putApprovalStep(Long key, List<ApprovalStepDetailUpdateResponse> value) {
		approvalStepRepository.put(key, value);
	}

}
