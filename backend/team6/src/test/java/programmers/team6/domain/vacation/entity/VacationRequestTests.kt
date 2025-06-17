package programmers.team6.domain.vacation.entity

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.vacation.entity.VacationRequest.Companion.create
import programmers.team6.domain.vacation.enums.VacationCode
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.support.MemberMother
import programmers.team6.support.TestVacationType
import java.time.LocalDate
import java.time.LocalDateTime

internal class VacationRequestTests {
    @Test
    @DisplayName("업데이트 성공테스트")
    fun update_success() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        val type: Code = Code(
            VacationCode.REWARD.groupCode,
            VacationCode.REWARD.code,
            VacationCode.REWARD.name
        )
        val start = LocalDate.of(2024, 10, 18).atStartOfDay()
        val end = LocalDate.of(2024, 10, 19).atStartOfDay()
        val updateReason = "updateReason"
        val approved = VacationRequestStatus.APPROVED

        vacationRequest.update(type, start, end, approved, updateReason)

        Assertions.assertThat(vacationRequest.type).isEqualTo(type)
        Assertions.assertThat(vacationRequest.status).isEqualTo(approved)
        Assertions.assertThat(vacationRequest.reason).isEqualTo(updateReason)
        Assertions.assertThat(vacationRequest.from).isEqualTo(start)
        Assertions.assertThat(vacationRequest.to).isEqualTo(end)
    }

    @Test
    @DisplayName("상태업데이트 성공테스트")
    fun updateStatus_success() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        val vacationRequestStatus = VacationRequestStatus.APPROVED

        vacationRequest.updateStatus(vacationRequestStatus)

        Assertions.assertThat(vacationRequest.status).isEqualTo(vacationRequestStatus)
    }

    @Test
    @DisplayName("업데이트 가능 검증 성공 테스트")
    fun canCancel_success() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        val result = vacationRequest.canCancel(1L)

        Assertions.assertThat(result).isTrue()
    }

    @Test
    @DisplayName("프로그래스상태가 아니면 업데이트는 실패한다")
    fun canUpdate_shouldFailed_ifStatusIsNotProgress() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.APPROVED,
            1
        )

        val result = vacationRequest.canCancel(1L)

        Assertions.assertThat(result).isFalse()
    }

    @Test
    @DisplayName("작성자가 아니면 업데이트는 실패한다")
    fun canUpdate_shouldFailed_ifAnotherMember() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        val result = vacationRequest.canCancel(2L)

        Assertions.assertThat(result).isFalse()
    }

    @Test
    @DisplayName("작성자면 업데이트에 성공한다")
    fun update_shouldSuccess_ifAnotherMember() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        val type: Code = Code(
            VacationCode.REWARD.groupCode, VacationCode.REWARD.code,
            VacationCode.REWARD.name
        )
        val start = LocalDate.of(2024, 10, 18).atStartOfDay()
        val end = LocalDate.of(2024, 10, 19).atStartOfDay()
        val updateReason = "updateReason"

        vacationRequest.updateByMember(1L, start, end, updateReason, type)

        Assertions.assertThat(vacationRequest.type).isEqualTo(type)
        Assertions.assertThat(vacationRequest.reason).isEqualTo(updateReason)
        Assertions.assertThat(vacationRequest.from).isEqualTo(start)
        Assertions.assertThat(vacationRequest.to).isEqualTo(end)
    }

    @Test
    @DisplayName("프로그래스상태가 아니면 업데이트는 실패한다")
    fun update_shouldFailed_ifStatusIsNotProgress() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.APPROVED,
            1
        )

        val type: Code = Code(
            VacationCode.REWARD.groupCode, VacationCode.REWARD.code,
            VacationCode.REWARD.name
        )
        val start = LocalDate.of(2024, 10, 18).atStartOfDay()
        val end = LocalDate.of(2024, 10, 19).atStartOfDay()
        val updateReason = "updateReason"

        Assertions.assertThatIllegalStateException().isThrownBy {
            vacationRequest.updateByMember(
                1L,
                start,
                end,
                updateReason,
                type
            )
        }
    }

    @Test
    @DisplayName("다른사용자가 업데이트하려면 업데이트는 실패한다")
    fun update_shouldFailed_ifAnotherMember() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        val type: Code = Code(
            VacationCode.REWARD.groupCode, VacationCode.REWARD.code,
            VacationCode.REWARD.name
        )
        val start = LocalDate.of(2024, 10, 18).atStartOfDay()
        val end = LocalDate.of(2024, 10, 19).atStartOfDay()
        val updateReason = "updateReason"

        Assertions.assertThatRuntimeException().isThrownBy {
            vacationRequest.updateByMember(
                2L,
                start,
                end,
                updateReason,
                type
            )
        }
    }

    @Test
    @DisplayName("취소가 성공한다")
    fun canCancel_shouldSuccess() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        val result = vacationRequest.canCancel(1L)

        Assertions.assertThat(result).isTrue()
    }

    @Test
    @DisplayName("작성자가 아니면 취소할 수 없다")
    fun canCancel_shouldFailed_ifAnotherMember() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        val result = vacationRequest.canCancel(2L)

        Assertions.assertThat(result).isFalse()
    }

    @Test
    @DisplayName("프로그래스 상태가 아니면 취소할 수 없다")
    fun canCancel_shouldFailed_ifStatusIsNotProgress() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.APPROVED,
            1
        )

        val result = vacationRequest.canCancel(1L)

        Assertions.assertThat(result).isFalse()
    }

    @Test
    @DisplayName("취소한다")
    fun cancel_shouldSuccess() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        vacationRequest.validateAndCancel(1L)

        Assertions.assertThat(vacationRequest.status).isEqualTo(VacationRequestStatus.CANCELED)
    }

    @Test
    @DisplayName("프로그래스 상태가 아니면 취소에 실패한다")
    fun cancel_shouldFailed_ifStatusIsNotProgress() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.APPROVED,
            1
        )

        Assertions.assertThatIllegalStateException().isThrownBy {
            vacationRequest.validateAndCancel(
                1L
            )
        }
    }

    @Test
    @DisplayName("작성자가 아니면 취소에 실패한다")
    fun cancel_shouldFailed_ifAnotherMember() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        Assertions.assertThatRuntimeException().isThrownBy { vacationRequest.validateAndCancel(2L) }
    }

    @Test
    @DisplayName("휴가를 허가한다")
    fun approve_shouldSuccess() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        vacationRequest.approve()

        Assertions.assertThat(vacationRequest.status).isEqualTo(VacationRequestStatus.APPROVED)
    }

    @Test
    @DisplayName("휴가를 반려한다")
    fun reject_shouldSuccess() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        vacationRequest.reject()

        Assertions.assertThat(vacationRequest.status).isEqualTo(VacationRequestStatus.REJECTED)
    }

    @Test
    @DisplayName("휴가를 반려한다")
    fun cancel_shouldSuccess2() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        vacationRequest.cancel()

        Assertions.assertThat(vacationRequest.status).isEqualTo(VacationRequestStatus.CANCELED)
    }

    @Test
    @DisplayName("휴가 사용 일수 계산")
    fun calc_vactions_days() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        val vacationDays = vacationRequest.calcVacationDays()

        Assertions.assertThat(vacationDays).isEqualTo(1L)
    }

    @Test
    @DisplayName("반차인 경우 true를 반환한다")
    fun is_half_true() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.HALF.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        val result = vacationRequest.isHalfDay()

        Assertions.assertThat(result).isTrue()
    }

    @Test
    @DisplayName("반차가 아닌경우 false를 반환한다")
    fun is_half_false() {
        val vacationRequest = create(
            MemberMother.withId(1L),
            LocalDateTime.now(),
            LocalDateTime.now(),
            "reason",
            TestVacationType.ANNUAL.toCode(),
            VacationRequestStatus.IN_PROGRESS,
            1
        )

        val result = vacationRequest.isHalfDay()

        Assertions.assertThat(result).isFalse()
    }
}