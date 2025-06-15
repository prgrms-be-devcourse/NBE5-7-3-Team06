package programmers.team6.domain.admin.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.response.AdminVacationRequestSearchResponse;
import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition;
import programmers.team6.domain.admin.dto.response.VacationRequestDetailReadResponse;
import programmers.team6.domain.admin.dto.request.VacationRequestDetailUpdateRequest;
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchCustom;
import programmers.team6.domain.admin.entity.Code;
import programmers.team6.domain.admin.repository.CodeRepository;
import programmers.team6.domain.vacation.entity.ApprovalStep;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.repository.ApprovalStepRepository;
import programmers.team6.domain.vacation.repository.VacationRequestRepository;
import programmers.team6.domain.vacation.support.VacationRequestReader;
import programmers.team6.global.exception.code.ConflictErrorCode;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.customException.ConflictException;
import programmers.team6.global.exception.customException.NotFoundException;

@Service
@RequiredArgsConstructor
public class AdminService {
	private final AdminVacationRequestSearchCustom adminVacationRequestSearchCustom;
	private final VacationRequestRepository vacationRequestRepository;
	private final CodeRepository codeRepository;
	private final ApprovalStepRepository approvalStepRepository;
	private final VacationRequestReader vacationRequestReader;

	@Transactional(readOnly = true)
	public AdminVacationRequestSearchResponse search(Pageable pageable, AdminVacationSearchCondition searchCondition) {
		return new AdminVacationRequestSearchResponse(
			adminVacationRequestSearchCustom.search(searchCondition, pageable),
			codeRepository.findCodeInfosByGroupCode("POSITION"),
			codeRepository.findCodeInfosByGroupCode("VACATION_TYPE"));
	}

	@Transactional(readOnly = true)
	public VacationRequestDetailReadResponse selectVacationRequestDetailById(Long id) {
		return vacationRequestReader.readDetailFrom(id);
	}

	@Transactional
	public void updateVacationRequestDetailById(Long id,
		VacationRequestDetailUpdateRequest vacationRequestDetailUpdateRequest) {
		VacationRequest vacationRequest = vacationRequestRepository.findVacationRequestById(id)
			.orElseThrow(() -> new NotFoundException(NotFoundErrorCode.NOT_FOUND_VACATION_REQUEST));

		Code vacationRequestType = codeRepository.findByIdAndGroupCode(vacationRequestDetailUpdateRequest.typeId(),
			"VACATION_TYPE").orElseThrow(() -> new NotFoundException(NotFoundErrorCode.NOT_FOUND_CODE));
		vacationRequest.update(vacationRequestType, vacationRequestDetailUpdateRequest.from(),
			vacationRequestDetailUpdateRequest.to(), vacationRequestDetailUpdateRequest.vacationRequestStatus(),
			vacationRequestDetailUpdateRequest.reason());

		List<ApprovalStep> approvalSteps = approvalStepRepository.findApprovalStepsByVacationRequest_IdOrderByStepAsc(
			id);
		if (approvalSteps.isEmpty()
			|| vacationRequestDetailUpdateRequest.approvalReason().size() != approvalSteps.size()) {
			throw new ConflictException(ConflictErrorCode.CONFLICT_APPROVAL_STEP);
		}
		for (int i = 0; i < approvalSteps.size(); i++) {
			approvalSteps.get(i).update(vacationRequestDetailUpdateRequest.approvalReason().get(i));
		}
	}
}
