package programmers.team6.domain.vacation.entity;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.EnumSource.Mode.*;
import static programmers.team6.domain.vacation.entity.util.ApprovalStepTestUtils.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import programmers.team6.domain.vacation.enums.ApprovalStatus;
import programmers.team6.global.exception.customException.BadRequestException;

class ApprovalStepTests {

	@Test
	@DisplayName("사유 업데이트시 정상적으로 변경된다")
	void update_test() throws Exception {
		ApprovalStep approvalStep = genApprovalStep(ApprovalStatus.PENDING);
		String reason = "~~해서 안됩니다.";

		approvalStep.update(reason);

		assertThat(approvalStep.getReason()).isEqualTo(reason);
	}

	@Test
	@DisplayName("승인 처리시 상태가 APPROVED로 변경된다")
	void approve_test() throws Exception {
		ApprovalStep approvalStep = genApprovalStep(ApprovalStatus.PENDING);

		approvalStep.approve();

		assertThat(approvalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
	}

	@Test
	@DisplayName("사유와 함께 반려 처리시 상태가 REJECTED로 변경된다")
	void reject_test() throws Exception {
		ApprovalStep approvalStep = genApprovalStep(ApprovalStatus.PENDING);
		String reason = "~~해서 안됩니다.";

		approvalStep.reject(reason);

		assertThat(approvalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
	}

	@Test
	@DisplayName("대기 처리시 상태가 PENDING 변경된다")
	void pending_test() throws Exception {
		ApprovalStep approvalStep = genApprovalStep(ApprovalStatus.WAITING);

		approvalStep.pending();

		assertThat(approvalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
	}

	@Test
	@DisplayName("취소 처리시 상태가 CANCELED로 변경된다")
	void calcel_test() throws Exception {
		ApprovalStep approvalStep = genApprovalStep(ApprovalStatus.CANCELED);

		approvalStep.cancel();

		assertThat(approvalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.CANCELED);
	}

	@Test
	@DisplayName("상태가 PENDING이면 승인검증 시 예외가 발생하지 않을 것이다.")
	void approvable_test() throws Exception {
		ApprovalStep approvalStep = genApprovalStep(ApprovalStatus.PENDING);

		assertDoesNotThrow(approvalStep::validateApprovable);
	}

	@ParameterizedTest
	@EnumSource(mode = EXCLUDE, names = "PENDING")
	@DisplayName("상태가 PENDING이 아니면 승인검증 시 예외가 발생할 것이다.")
	void un_approvable_test(ApprovalStatus status) throws Exception {
		ApprovalStep approvalStep = genApprovalStep(status);

		assertThrows(
			BadRequestException.class,
			approvalStep::validateApprovable
		);

	}

	@Test
	@DisplayName("상태가 PENDING이면 반려검증 시 예외가 발생하지 않을 것이다.")
	void rejectable_test() throws Exception {
		ApprovalStep approvalStep = genApprovalStep(ApprovalStatus.PENDING);

		assertDoesNotThrow(approvalStep::validateRejectable);
	}

	@ParameterizedTest
	@EnumSource(mode = EXCLUDE, names = "PENDING")
	@DisplayName("상태가 PENDING이 아니면 반려검증 시 예외가 발생할 것이다.")
	void un_rejectable_test(ApprovalStatus status) throws Exception {
		ApprovalStep approvalStep = genApprovalStep(status);

		assertThrows(
			BadRequestException.class,
			approvalStep::validateRejectable
		);

	}

}