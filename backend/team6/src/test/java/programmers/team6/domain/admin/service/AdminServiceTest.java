package programmers.team6.domain.admin.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import programmers.team6.domain.admin.dto.VacationRequestDetailUpdateRequest;
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchCustom;
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchTestDataFactory;
import programmers.team6.domain.member.entity.Code;
import programmers.team6.domain.member.repository.CodeRepository;
import programmers.team6.domain.vacation.entity.ApprovalStep;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;
import programmers.team6.domain.vacation.repository.ApprovalStepRepository;
import programmers.team6.domain.vacation.repository.VacationRequestRepository;
import programmers.team6.domain.vacation.service.VacationRequestReader;
import programmers.team6.global.exception.code.ConflictErrorCode;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.customException.ConflictException;
import programmers.team6.global.exception.customException.NotFoundException;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
	@Mock
	AdminVacationRequestSearchCustom adminVacationRequestSearchCustom;
	@Mock
	VacationRequestRepository vacationRequestRepository;
	@Mock
	CodeRepository codeRepository;
	@Mock
	ApprovalStepRepository approvalStepRepository;
	@Mock
	VacationRequestReader vacationRequestReader;
	@InjectMocks
	AdminService adminService;

	@Nested
	@DisplayName("VacationRequestDetail 수정 과정에서 ")
	class should_updateVacationRequestDetail {
		private VacationRequest vacationRequest;
		private Code vacationRequestType;
		private VacationRequestDetailUpdateRequest vacationRequestDetailUpdateRequest;
		private List<ApprovalStep> approvalSteps;

		@BeforeEach
		void setUp() {
			this.vacationRequest = VacationRequest.builder().build();
			this.vacationRequestType = Code.builder()
				.groupCode("VACATION_TYPE")
				.code(UUID.randomUUID().toString())
				.name("test_name")
				.build();
			this.vacationRequestDetailUpdateRequest = new VacationRequestDetailUpdateRequest(0L,
				LocalDateTime.now().plusDays(1L), LocalDateTime.now().plusDays(3L), VacationRequestStatus.APPROVED,
				"testReason", List.of("r1", "r2", "r3"));
			this.approvalSteps = new ArrayList<>();
			for (int i = 0; i < 3; i++) {
				approvalSteps.add(
					AdminVacationRequestSearchTestDataFactory.genTestApprovalStep(vacationRequest, i, String.format("o%d", i)));
			}
		}

		@Test
		@DisplayName("알맞은 VacationRequestId와 VacationRequestDetail 입력시, update 성공")
		void success_when_givenValidVacationRequestIdAndVacationRequestDetail() {
			// when
			when(vacationRequestRepository.findVacationRequestById(anyLong())).thenReturn(Optional.of(vacationRequest));
			when(codeRepository.findByIdAndGroupCode(anyLong(), eq("VACATION_TYPE"))).thenReturn(
				Optional.of(vacationRequestType));
			when(approvalStepRepository.findApprovalStepsByVacationRequest_IdOrderByStepAsc(anyLong())).thenReturn(
				approvalSteps);
			adminService.updateVacationRequestDetailById(0L, vacationRequestDetailUpdateRequest);

			// then
			assertThat(vacationRequest.getFrom()).isEqualTo(vacationRequestDetailUpdateRequest.from());
			assertThat(vacationRequest.getTo()).isEqualTo(vacationRequestDetailUpdateRequest.to());
			assertThat(vacationRequest.getStatus()).isEqualTo(
				vacationRequestDetailUpdateRequest.vacationRequestStatus());
			assertThat(vacationRequest.getReason()).isEqualTo(vacationRequestDetailUpdateRequest.reason());
			assertThat(approvalSteps).hasSize(3).extracting(ApprovalStep::getReason).containsExactly("r1", "r2", "r3");
		}

		@Test
		@DisplayName("잘못된 VacationRequestId 입력시, NotFoundException 발생")
		void fail_when_givenInvalidVacationRequestId() {
			// when
			when(vacationRequestRepository.findVacationRequestById(anyLong())).thenReturn(Optional.empty());

			// then
			assertThatThrownBy(
				() -> adminService.updateVacationRequestDetailById(0L, vacationRequestDetailUpdateRequest))
				.isInstanceOf(NotFoundException.class)
				.hasMessage(NotFoundErrorCode.NOT_FOUND_VACATION_REQUEST.getMessage());
		}

		@Test
		@DisplayName("잘못된 VacationRequest의 typeId(분류코드 id) 입력시, NotFoundException 발생")
		void fail_when_givenInvalidVacationRequestTypeId() {
			// when
			when(vacationRequestRepository.findVacationRequestById(anyLong())).thenReturn(Optional.of(vacationRequest));
			when(codeRepository.findByIdAndGroupCode(anyLong(), eq("VACATION_TYPE"))).thenReturn(Optional.empty());

			// then
			assertThatThrownBy(
				() -> adminService.updateVacationRequestDetailById(0L, vacationRequestDetailUpdateRequest))
				.isInstanceOf(NotFoundException.class)
				.hasMessage(NotFoundErrorCode.NOT_FOUND_CODE.getMessage());
		}

		@Test
		@DisplayName("해당 VacationReuqest의 ApprovalStep이 없을 경우, ConflictException 발생")
		void fail_when_givenEmptyApprovalSteps() {
			// when
			when(vacationRequestRepository.findVacationRequestById(anyLong())).thenReturn(Optional.of(vacationRequest));
			when(codeRepository.findByIdAndGroupCode(anyLong(), eq("VACATION_TYPE"))).thenReturn(
				Optional.of(vacationRequestType));
			when(approvalStepRepository.findApprovalStepsByVacationRequest_IdOrderByStepAsc(anyLong())).thenReturn(
				Collections.emptyList());

			// then
			assertThatThrownBy(
				() -> adminService.updateVacationRequestDetailById(0L, vacationRequestDetailUpdateRequest))
				.isInstanceOf(ConflictException.class)
				.hasMessage(ConflictErrorCode.CONFLICT_APPROVAL_STEP.getMessage());
		}

		@Test
		@DisplayName("해당 VacationReuqest와 ApprovalStep가 동기화가 안된경우, ConflictException 발생")
		void fail_when_givenInvalidApprovalSteps() {
			// when
			when(vacationRequestRepository.findVacationRequestById(anyLong())).thenReturn(Optional.of(vacationRequest));
			when(codeRepository.findByIdAndGroupCode(anyLong(), eq("VACATION_TYPE"))).thenReturn(
				Optional.of(vacationRequestType));
			when(approvalStepRepository.findApprovalStepsByVacationRequest_IdOrderByStepAsc(anyLong())).thenReturn(
				List.of(AdminVacationRequestSearchTestDataFactory.genTestApprovalStep(null, 0, null)));

			// then
			assertThatThrownBy(
				() -> adminService.updateVacationRequestDetailById(0L, vacationRequestDetailUpdateRequest))
				.isInstanceOf(ConflictException.class)
				.hasMessage(ConflictErrorCode.CONFLICT_APPROVAL_STEP.getMessage());
		}
	}
}