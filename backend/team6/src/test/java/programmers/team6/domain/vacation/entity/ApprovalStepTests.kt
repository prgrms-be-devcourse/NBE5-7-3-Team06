package programmers.team6.domain.vacation.entity

import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import programmers.team6.domain.vacation.entity.util.ApprovalStepTestUtils
import programmers.team6.domain.vacation.enums.ApprovalStatus
import programmers.team6.global.exception.customException.BadRequestException

internal class ApprovalStepTests {
    @Test
    @DisplayName("사유 업데이트시 정상적으로 변경된다")
    fun update_test() {
        val approvalStep = ApprovalStepTestUtils.genApprovalStep(ApprovalStatus.PENDING)
        val reason = "~~해서 안됩니다."

        approvalStep.update(reason)

        AssertionsForClassTypes.assertThat(approvalStep.reason).isEqualTo(reason)
    }

    @Test
    @DisplayName("승인 처리시 상태가 APPROVED로 변경된다")
    fun approve_test() {
        val approvalStep = ApprovalStepTestUtils.genApprovalStep(ApprovalStatus.PENDING)

        approvalStep.approve()

        AssertionsForClassTypes.assertThat(approvalStep.approvalStatus).isEqualTo(ApprovalStatus.APPROVED)
    }

    @Test
    @DisplayName("사유와 함께 반려 처리시 상태가 REJECTED로 변경된다")
    fun reject_test() {
        val approvalStep = ApprovalStepTestUtils.genApprovalStep(ApprovalStatus.PENDING)
        val reason = "~~해서 안됩니다."

        approvalStep.reject(reason)

        AssertionsForClassTypes.assertThat(approvalStep.approvalStatus).isEqualTo(ApprovalStatus.REJECTED)
    }

    @Test
    @DisplayName("대기 처리시 상태가 PENDING 변경된다")
    fun pending_test() {
        val approvalStep = ApprovalStepTestUtils.genApprovalStep(ApprovalStatus.WAITING)

        approvalStep.pending()

        AssertionsForClassTypes.assertThat(approvalStep.approvalStatus).isEqualTo(ApprovalStatus.PENDING)
    }

    @Test
    @DisplayName("취소 처리시 상태가 CANCELED로 변경된다")
    fun calcel_test() {
        val approvalStep = ApprovalStepTestUtils.genApprovalStep(ApprovalStatus.CANCELED)

        approvalStep.cancel()

        AssertionsForClassTypes.assertThat(approvalStep.approvalStatus).isEqualTo(ApprovalStatus.CANCELED)
    }

    @Test
    @DisplayName("상태가 PENDING이면 승인검증 시 예외가 발생하지 않을 것이다.")
    fun approvable_test() {
        val approvalStep = ApprovalStepTestUtils.genApprovalStep(ApprovalStatus.PENDING)

        Assertions.assertDoesNotThrow { approvalStep.validateApprovable() }
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["PENDING"])
    @DisplayName("상태가 PENDING이 아니면 승인검증 시 예외가 발생할 것이다.")
    fun un_approvable_test(status: ApprovalStatus) {
        val approvalStep = ApprovalStepTestUtils.genApprovalStep(status)

        Assertions.assertThrows(
            BadRequestException::class.java
        ) { approvalStep.validateApprovable() }
    }

    @Test
    @DisplayName("상태가 PENDING이면 반려검증 시 예외가 발생하지 않을 것이다.")
    fun rejectable_test() {
        val approvalStep = ApprovalStepTestUtils.genApprovalStep(ApprovalStatus.PENDING)

        Assertions.assertDoesNotThrow { approvalStep.validateRejectable() }
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["PENDING"])
    @DisplayName("상태가 PENDING이 아니면 반려검증 시 예외가 발생할 것이다.")
    fun un_rejectable_test(status: ApprovalStatus) {
        val approvalStep = ApprovalStepTestUtils.genApprovalStep(status)

        Assertions.assertThrows(
            BadRequestException::class.java
        ) { approvalStep.validateRejectable() }
    }
}