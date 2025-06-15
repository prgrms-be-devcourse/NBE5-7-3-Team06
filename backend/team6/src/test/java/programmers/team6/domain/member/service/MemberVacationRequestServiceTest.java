package programmers.team6.domain.member.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import programmers.team6.domain.admin.dto.response.ApprovalStepDetailUpdateResponse;
import programmers.team6.domain.admin.dto.response.VacationRequestDetailReadResponse;
import programmers.team6.domain.vacation.entity.ApprovalStep;
import programmers.team6.domain.vacation.enums.ApprovalStatus;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.customException.ForbiddenException;
import programmers.team6.global.exception.customException.NotFoundException;
import programmers.team6.mock.VacationRequestReaderFake;

/**
 * 성공 테스트에 기존의 오브젝트 마더 패턴을 적용하려했지만 merge과정의 conflict를 우려해서 우선 Low하게 테스트 진행,
 * 이후 마이그레이션 이후에 테스트 수정 필요
 * @author gunwoong
 */
class MemberVacationRequestServiceTest {
	private VacationRequestReaderFake vacationRequestReaderFake;

	@BeforeEach
	void setUP() {
		vacationRequestReaderFake = new VacationRequestReaderFake();
	}

	@Test
	@DisplayName("휴가계 id와 유저 id 제공시, 휴가계 디테일 반환")
	void should_selectVacationRequestDetailById_when_givenVacationRequestIdAndMemberId() {
		// given & when
		Long vacationRequestId = 0L;
		Long memberId = 0L;
		LocalDateTime from = LocalDateTime.now();
		LocalDateTime to = from.plusDays(1);
		String name = "testName";
		String deptName = "testDeptName";
		String posiiton = "testPosition";
		String reason = "testReason";
		String vacationType = "vacationType";
		VacationRequestStatus status = VacationRequestStatus.IN_PROGRESS;

		// when
		vacationRequestReaderFake.putVacationRequestDetail(vacationRequestId,
			new VacationRequestDetailReadResponse(vacationRequestId, from, to, memberId, name, deptName, posiiton,
				reason, vacationType, status,Collections.emptyList()));

		vacationRequestReaderFake.putApprovalStep(vacationRequestId,
			List.of(new ApprovalStepDetailUpdateResponse("name", "reason", ApprovalStatus.PENDING)));
		MemberVacationRequestService memberVacationRequestService = new MemberVacationRequestService(
			vacationRequestReaderFake);

		// then
		VacationRequestDetailReadResponse result = memberVacationRequestService.selectVacationRequestDetailById(
			vacationRequestId, memberId);
		assertThat(result).extracting(VacationRequestDetailReadResponse::getId, VacationRequestDetailReadResponse::getFrom,
				VacationRequestDetailReadResponse::getTo, VacationRequestDetailReadResponse::getMemberId,
				VacationRequestDetailReadResponse::getName, VacationRequestDetailReadResponse::getDeptName,
				VacationRequestDetailReadResponse::getPosition, VacationRequestDetailReadResponse::getReason,
				VacationRequestDetailReadResponse::getVacationType, VacationRequestDetailReadResponse::getVacationRequestStatus)
			.containsExactly(memberId, from, to, memberId, name, deptName, posiiton, reason, vacationType, status);
		assertThat(result.getApprovalStepDetailUpdateResponses()).hasSize(1);
	}

	@Test
	@DisplayName("존재하지 않는 VacationRequestId 제공시, NotFoundException 발생")
	void should_throwForbiddenException_when_givenNotExistVacationRequestId() {
		// given & when
		MemberVacationRequestService memberVacationRequestService = new MemberVacationRequestService(
			vacationRequestReaderFake);

		// then
		assertThatThrownBy(() -> memberVacationRequestService.selectVacationRequestDetailById(0L, 0L)).isInstanceOf(
			NotFoundException.class).hasMessage(NotFoundErrorCode.NOT_FOUND_VACATION_REQUEST.getMessage());
	}

	@Test
	@DisplayName("찾으려는 VacationRequest의 ApprovalSteps가 존재하지 않을 경우, NotFoundException 발생")
	void should_throwNotFoundException_when_vacationRequestHasEmptyApprovalSteps() {
		// given & when
		vacationRequestReaderFake.putVacationRequestDetail(0L,
			new VacationRequestDetailReadResponse(0L, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0l, "", "", "", "", "", VacationRequestStatus.IN_PROGRESS,
				Collections.emptyList()));

		MemberVacationRequestService memberVacationRequestService = new MemberVacationRequestService(
			vacationRequestReaderFake);

		// then
		assertThatThrownBy(() -> memberVacationRequestService.selectVacationRequestDetailById(0L, 0L)).isInstanceOf(
			NotFoundException.class).hasMessage(NotFoundErrorCode.NOT_FOUND_APPROVAL_STEP.getMessage());
	}

	@Test
	@DisplayName("입력된 memberId와 조회된 memberId가 다를 경우, ForbiddenException 발생")
	void should_throwForbiddenException_when_givenNotEqualMemberId() {
		Long givenMemberId = 0L;
		Long findMemberId = 1L;
		vacationRequestReaderFake.putVacationRequestDetail(0L,
			new VacationRequestDetailReadResponse(0L, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
				findMemberId, "", "", "", "", "", VacationRequestStatus.IN_PROGRESS,
				List.of(new ApprovalStepDetailUpdateResponse("a", "b", ApprovalStatus.PENDING))));

		vacationRequestReaderFake.putApprovalStep(0L, List.of(new ApprovalStepDetailUpdateResponse(null, null, null)));

		MemberVacationRequestService memberVacationRequestService = new MemberVacationRequestService(
			vacationRequestReaderFake);

		// then
		assertThatThrownBy(
			() -> memberVacationRequestService.selectVacationRequestDetailById(0L, givenMemberId)).isInstanceOf(
			ForbiddenException.class);
	}

}