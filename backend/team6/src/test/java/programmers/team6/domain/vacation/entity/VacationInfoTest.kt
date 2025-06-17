package programmers.team6.domain.vacation.entity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import programmers.team6.global.exception.customException.BadRequestException

internal class VacationInfoTest {
    @Test
    fun 휴가정보업데이트() {
        val info = VacationInfo(15.0, 13.0, "test", 1L)
        val updateTotalCount = 13.0

        val result = info.updateTotalCount(updateTotalCount)

        assertThat(updateTotalCount).isEqualTo(result.totalCount).isEqualTo(info.totalCount)
    }

    @Test
    fun 사용휴가정보보다_적게_부여휴가를_비업데이트() {
        val totalCount = 15.0
        val info = VacationInfo(totalCount, 13.0, "test", 1L)
        val updateTotalCount = 12.0

        assertThatThrownBy { info.updateTotalCount(updateTotalCount) }.isInstanceOf(
            RuntimeException::class.java
        )
    }

    // updateTotalCount() 관련 test
    @Test
    @DisplayName("총 휴가일수 업데이트 성공")
    fun update_total_count_success() {
        val newTotalCount = 5.0

        val vacationInfo = VacationInfo(15.0, 5.0, "01", 1L)

        val log = vacationInfo.updateTotalCount(newTotalCount)

        assertThat(vacationInfo.totalCount).isEqualTo(newTotalCount)
        assertThat(log.totalCount).isEqualTo(newTotalCount)
    }

    @Test
    @DisplayName("사용일수보다 적은 총일수로 업데이트하면 예외 발생")
    fun update_total_count_failure() {
        val newTotalCount = 9.0

        val vacationInfo = VacationInfo(15.0, 10.0, "01", 1L)

        assertThatThrownBy { vacationInfo.updateTotalCount(newTotalCount) }
            .isInstanceOf(BadRequestException::class.java)
    }

    // init() 관련 test
    @Test
    @DisplayName("휴가 정보 초기화 - 총일수 변경 및 사용일수 0")
    fun init_vacation_info_total_count_and_use_count() {
        val newTotalCount = 10.0
        val expectedUseCount = 0.0

        val vacationInfo = VacationInfo(15.0, 10.0, "01", 1L)

        val log = vacationInfo.init(newTotalCount)

        assertThat(vacationInfo.totalCount).isEqualTo(newTotalCount)
        assertThat(vacationInfo.useCount).isEqualTo(expectedUseCount)
        assertThat(log.totalCount).isEqualTo(newTotalCount)
        assertThat(log.useCount).isEqualTo(expectedUseCount)
    }

    @Test
    @DisplayName("휴가 정보 초기화 - 음수 총일수")
    fun init_vacation_info_negative_total_count() {
        val negativeTotalCount = -5.0

        val vacationInfo = VacationInfo(15.0, 10.0, "01", 1L)

        assertThatThrownBy { vacationInfo.init(negativeTotalCount) }
            .isInstanceOf(BadRequestException::class.java)
    }

    // useVacation() 관련 test
    @Test
    @DisplayName("휴가 사용 시 사용일수 정상 증가")
    fun use_vacation_success() {
        val initialUseCount = 5.0
        val additionalUseCount = 10.0
        val expectedUseCount = initialUseCount + additionalUseCount

        val vacationInfo = VacationInfo(15.0, initialUseCount, "01", 1L)

        val log = vacationInfo.useVacation(additionalUseCount)

        assertThat(vacationInfo.useCount).isEqualTo(expectedUseCount)
        assertThat(log.useCount).isEqualTo(expectedUseCount)
    }

    @Test
    @DisplayName("잔여일수보다 많이 사용하려 하면 예외 발생")
    fun use_vacation_failure() {
        val useCount = 6.0

        val vacationInfo = VacationInfo(15.0, 10.0, "01", 1L)

        assertThrows<BadRequestException> {
            vacationInfo.useVacation(useCount)
        }
    }

    // isSameVersion() 관련 test
    @Test
    @DisplayName("버전 비교 테스트 - 동일한 버전")
    fun is_same_version_when_equal() {
        val version = 0

        val vacationInfo = VacationInfo(15.0, 10.0, "01", 1L)

        assertThat(vacationInfo.isSameVersion(version)).isTrue()
    }

    @Test
    @DisplayName("버전 비교 테스트 - 다른 버전")
    fun is_same_version_when_different() {
        val version = 1

        val vacationInfo = VacationInfo(15.0, 10.0, "01", 1L)

        assertThat(vacationInfo.isSameVersion(version)).isFalse()
    }

    // canUseVacation() 관련 test
    @Test
    @DisplayName("휴가 사용 검증 - 사용 가능한 경우")
    fun can_use_vacation_when_sufficient() {
        val count = 10.0

        val vacationInfo = VacationInfo(15.0, 5.0, "01", 1L)

        assertThat(vacationInfo.canUseVacation(count)).isTrue()
    }

    @Test
    @DisplayName("휴가 사용 검증 - 사용 불가능한 경우")
    fun can_use_vacation_when_insufficient() {
        val count = 11.0

        val vacationInfo = VacationInfo(15.0, 5.0, "01", 1L)

        assertThat(vacationInfo.canUseVacation(count)).isFalse()
    }

    // toLog() 관련 test
    @Test
    @DisplayName("VacationInfoLog 생성 확인")
    fun to_log_create() {
        val totalCount = 15.0
        val useCount = 5.0
        val vacationType = "01"
        val memberId = 1L
        val remainingCount = totalCount - useCount

        val vacationInfo = VacationInfo(totalCount, useCount, vacationType, memberId)

        val log = vacationInfo.toLog()

        assertThat(log.totalCount).isEqualTo(totalCount)
        assertThat(log.useCount).isEqualTo(useCount)
        assertThat(log.vacationType).isEqualTo(vacationType)
        assertThat(log.memberId).isEqualTo(memberId)
        assertThat(log.logDate).isNotNull()
        assertThat(log.remainingCount()).isEqualTo(remainingCount)
    }
}