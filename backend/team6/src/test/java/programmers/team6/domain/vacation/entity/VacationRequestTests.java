package programmers.team6.domain.vacation.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import programmers.team6.domain.member.entity.Code;
import programmers.team6.domain.vacation.enums.VacationCode;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;
import programmers.team6.support.MemberMother;
import programmers.team6.support.TestVacationType;

class VacationRequestTests {

	@Test
	@DisplayName("업데이트 성공테스트")
	void update_success() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();
		Code type = new Code(VacationCode.REWARD.getGroupCode(), VacationCode.REWARD.getCode(),
			VacationCode.REWARD.name());
		LocalDateTime start = LocalDate.of(2024, 10, 18).atStartOfDay();
		LocalDateTime end = LocalDate.of(2024, 10, 19).atStartOfDay();
		String updateReason = "updateReason";
		VacationRequestStatus approved = VacationRequestStatus.APPROVED;

		vacationRequest.update(type, start, end, approved, updateReason);

		assertThat(vacationRequest.getType()).isEqualTo(type);
		assertThat(vacationRequest.getStatus()).isEqualTo(approved);
		assertThat(vacationRequest.getReason()).isEqualTo(updateReason);
		assertThat(vacationRequest.getFrom()).isEqualTo(start);
		assertThat(vacationRequest.getTo()).isEqualTo(end);
	}

	@Test
	@DisplayName("상태업데이트 성공테스트")
	void updateStatus_success() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();
		VacationRequestStatus vacationRequestStatus = VacationRequestStatus.APPROVED;

		vacationRequest.updateStatus(vacationRequestStatus);

		assertThat(vacationRequest.getStatus()).isEqualTo(vacationRequestStatus);
	}

	@Test
	@DisplayName("업데이트 가능 검증 성공 테스트")
	void canCancel_success() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();

		boolean result = vacationRequest.canCancel(1L);

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("프로그래스상태가 아니면 업데이트는 실패한다")
	void canUpdate_shouldFailed_ifStatusIsNotProgress() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.APPROVED)
			.version(1)
			.build();

		boolean result = vacationRequest.canCancel(1L);

		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("작성자가 아니면 업데이트는 실패한다")
	void canUpdate_shouldFailed_ifAnotherMember() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();

		boolean result = vacationRequest.canCancel(2L);

		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("작성자면 업데이트에 성공한다")
	void update_shouldSuccess_ifAnotherMember() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();
		Code type = new Code(VacationCode.REWARD.getGroupCode(), VacationCode.REWARD.getCode(),
			VacationCode.REWARD.name());
		LocalDateTime start = LocalDate.of(2024, 10, 18).atStartOfDay();
		LocalDateTime end = LocalDate.of(2024, 10, 19).atStartOfDay();
		String updateReason = "updateReason";

		vacationRequest.updateByMember(1L, start, end, updateReason, type);

		assertThat(vacationRequest.getType()).isEqualTo(type);
		assertThat(vacationRequest.getReason()).isEqualTo(updateReason);
		assertThat(vacationRequest.getFrom()).isEqualTo(start);
		assertThat(vacationRequest.getTo()).isEqualTo(end);
	}

	@Test
	@DisplayName("프로그래스상태가 아니면 업데이트는 실패한다")
	void update_shouldFailed_ifStatusIsNotProgress() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.APPROVED)
			.version(1)
			.build();
		Code type = new Code(VacationCode.REWARD.getGroupCode(), VacationCode.REWARD.getCode(),
			VacationCode.REWARD.name());
		LocalDateTime start = LocalDate.of(2024, 10, 18).atStartOfDay();
		LocalDateTime end = LocalDate.of(2024, 10, 19).atStartOfDay();
		String updateReason = "updateReason";

		assertThatIllegalStateException().isThrownBy(
			() -> vacationRequest.updateByMember(1L, start, end, updateReason, type));
	}

	@Test
	@DisplayName("다른사용자가 업데이트하려면 업데이트는 실패한다")
	void update_shouldFailed_ifAnotherMember() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();
		Code type = new Code(VacationCode.REWARD.getGroupCode(), VacationCode.REWARD.getCode(),
			VacationCode.REWARD.name());
		LocalDateTime start = LocalDate.of(2024, 10, 18).atStartOfDay();
		LocalDateTime end = LocalDate.of(2024, 10, 19).atStartOfDay();
		String updateReason = "updateReason";

		assertThatRuntimeException().isThrownBy(
			() -> vacationRequest.updateByMember(2L, start, end, updateReason, type));
	}

	@Test
	@DisplayName("취소가 성공한다")
	void canCancel_shouldSuccess() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();

		boolean result = vacationRequest.canCancel(1L);

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("작성자가 아니면 취소할 수 없다")
	void canCancel_shouldFailed_ifAnotherMember() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();

		boolean result = vacationRequest.canCancel(2L);

		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("프로그래스 상태가 아니면 취소할 수 없다")
	void canCancel_shouldFailed_ifStatusIsNotProgress() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.APPROVED)
			.version(1)
			.build();

		boolean result = vacationRequest.canCancel(1L);

		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("취소한다")
	void cancel_shouldSuccess() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();

		vacationRequest.validateAndCancel(1L);

		assertThat(vacationRequest.getStatus()).isEqualTo(VacationRequestStatus.CANCELED);

	}

	@Test
	@DisplayName("프로그래스 상태가 아니면 취소에 실패한다")
	void cancel_shouldFailed_ifStatusIsNotProgress() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.APPROVED)
			.version(1)
			.build();

		assertThatIllegalStateException().isThrownBy(() -> vacationRequest.validateAndCancel(1L));
	}

	@Test
	@DisplayName("작성자가 아니면 취소에 실패한다")
	void cancel_shouldFailed_ifAnotherMember() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();

		assertThatRuntimeException().isThrownBy(() -> vacationRequest.validateAndCancel(2L));
	}

	@Test
	@DisplayName("휴가를 허가한다")
	void approve_shouldSuccess() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();

		vacationRequest.approve();

		assertThat(vacationRequest.getStatus()).isEqualTo(VacationRequestStatus.APPROVED);
	}

	@Test
	@DisplayName("휴가를 반려한다")
	void reject_shouldSuccess() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();

		vacationRequest.reject();

		assertThat(vacationRequest.getStatus()).isEqualTo(VacationRequestStatus.REJECTED);
	}

	@Test
	@DisplayName("휴가를 반려한다")
	void cancel_shouldSuccess2() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();

		vacationRequest.cancel();

		assertThat(vacationRequest.getStatus()).isEqualTo(VacationRequestStatus.CANCELED);
	}

	@Test
	@DisplayName("휴가 사용 일수 계산")
	void calc_vactions_days() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();

		int vacationDays = vacationRequest.calcVacationDays();

		assertThat(vacationDays).isEqualTo(1L);
	}

	@Test
	@DisplayName("반차인 경우 true를 반환한다")
	void is_half_true() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.HALP.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();

		boolean result = vacationRequest.isHalfDay();

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("반차가 아닌경우 false를 반환한다")
	void is_half_false() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.member(MemberMother.withId(1L))
			.from(LocalDateTime.now())
			.to(LocalDateTime.now())
			.reason("reason")
			.type(TestVacationType.ANNUAL.toCode())
			.status(VacationRequestStatus.IN_PROGRESS)
			.version(1)
			.build();

		boolean result = vacationRequest.isHalfDay();

		assertThat(result).isFalse();
	}
}