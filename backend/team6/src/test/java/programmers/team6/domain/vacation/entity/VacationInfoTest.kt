package programmers.team6.domain.vacation.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import programmers.team6.global.exception.customException.BadRequestException;

class VacationInfoTest {

	@Test
	void 휴가정보업데이트() {
		VacationInfo info = new VacationInfo(15, 13, "test", 1L);
		double updateTotalCount = 13;

		VacationInfoLog result = info.updateTotalCount(updateTotalCount);

		assertThat(updateTotalCount).isEqualTo(result.getTotalCount()).isEqualTo(info.getTotalCount());
	}

	@Test
	void 사용휴가정보보다_적게_부여휴가를_비업데이트() {
		double totalCount = 15;
		VacationInfo info = new VacationInfo(totalCount, 13, "test", 1L);
		double updateTotalCount = 12;

		assertThatThrownBy(() -> info.updateTotalCount(updateTotalCount)).isInstanceOf(RuntimeException.class);
	}

	// updateTotalCount() 관련 test
	@Test
	@DisplayName("총 휴가일수 업데이트 성공")
	void update_total_count_success() {

		double newTotalCount = 5.0;

		VacationInfo vacationInfo = new VacationInfo(15.0, 5.0, "01", 1L);

		VacationInfoLog log = vacationInfo.updateTotalCount(newTotalCount);

		assertThat(vacationInfo.getTotalCount()).isEqualTo(newTotalCount);
		assertThat(log.getTotalCount()).isEqualTo(newTotalCount);

	}

	@Test
	@DisplayName("사용일수보다 적은 총일수로 업데이트하면 예외 발생")
	void update_total_count_failure() {

		double newTotalCount = 9.0;

		VacationInfo vacationInfo = new VacationInfo(15.0, 10.0, "01", 1L);

		assertThatThrownBy(() -> vacationInfo.updateTotalCount(newTotalCount))
			.isInstanceOf(BadRequestException.class);

	}

	// init() 관련 test
	@Test
	@DisplayName("휴가 정보 초기화 - 총일수 변경 및 사용일수 0")
	void init_vacation_info_total_count_and_use_count() {

		double newTotalCount = 10.0;
		double expectedUseCount = 0;

		VacationInfo vacationInfo = new VacationInfo(15.0, 10.0, "01", 1L);

		VacationInfoLog log = vacationInfo.init(newTotalCount);

		assertThat(vacationInfo.getTotalCount()).isEqualTo(newTotalCount);
		assertThat(vacationInfo.getUseCount()).isEqualTo(expectedUseCount);
		assertThat(log.getTotalCount()).isEqualTo(newTotalCount);
		assertThat(log.getUseCount()).isEqualTo(expectedUseCount);

	}
	
	@Test
	@DisplayName("휴가 정보 초기화 - 음수 총일수")
	void init_vacation_info_negative_total_count() {

		double negativeTotalCount = -5.0;

		VacationInfo vacationInfo = new VacationInfo(15.0, 10.0, "01", 1L);

		assertThatThrownBy(() -> vacationInfo.init(negativeTotalCount))
			.isInstanceOf(BadRequestException.class);

	}

	// useVacation() 관련 test
	@Test
	@DisplayName("휴가 사용 시 사용일수 정상 증가")
	void use_vacation_success() {

		double initialUseCount = 5.0;
		double additionalUseCount = 10.0;
		double expectedUseCount = initialUseCount + additionalUseCount;

		VacationInfo vacationInfo = new VacationInfo(15.0, initialUseCount, "01", 1L);

		VacationInfoLog log = vacationInfo.useVacation(additionalUseCount);

		assertThat(vacationInfo.getUseCount()).isEqualTo(expectedUseCount);
		assertThat(log.getUseCount()).isEqualTo(expectedUseCount);

	}

	@Test
	@DisplayName("잔여일수보다 많이 사용하려 하면 예외 발생")
	void use_vacation_failure() {

		double useCount = 6.0;

		VacationInfo vacationInfo = new VacationInfo(15.0, 10.0, "01", 1L);

		assertThatThrownBy(() -> vacationInfo.useVacation(useCount))
			.isInstanceOf(BadRequestException.class);

	}

	// isSameVersion() 관련 test
	@Test
	@DisplayName("버전 비교 테스트 - 동일한 버전")
	void is_same_version_when_equal() {

		Integer version = 0;

	    VacationInfo vacationInfo = new VacationInfo(15.0, 10.0, "01", 1L);

		assertThat(vacationInfo.isSameVersion(version)).isTrue();

	}

	@Test
	@DisplayName("버전 비교 테스트 - 다른 버전")
	void is_same_version_when_different() {

		Integer version = 1;

	    VacationInfo vacationInfo = new VacationInfo(15.0, 10.0, "01", 1L);

		assertThat(vacationInfo.isSameVersion(version)).isFalse();

	}

	// canUseVacation() 관련 test
	@Test
	@DisplayName("휴가 사용 검증 - 사용 가능한 경우")
	void can_use_vacation_when_sufficient() {

		double count = 10.0;

		VacationInfo vacationInfo = new VacationInfo(15.0, 5.0, "01", 1L);

		assertThat(vacationInfo.canUseVacation(count)).isTrue();

	}

	@Test
	@DisplayName("휴가 사용 검증 - 사용 불가능한 경우")
	void can_use_vacation_when_insufficient() {

		double count = 11.0;

	    VacationInfo vacationInfo = new VacationInfo(15.0, 5.0, "01", 1L);

		assertThat(vacationInfo.canUseVacation(count)).isFalse();

	}

	// toLog() 관련 test
	@Test
	@DisplayName("VacationInfoLog 생성 확인")
	void to_log_create() {

		double totalCount = 15.0;
		double useCount = 5.0;
		String vacationType = "01";
		Long memberId = 1L;
		double remainingCount = totalCount - useCount;

	    VacationInfo vacationInfo = new VacationInfo(totalCount, useCount, vacationType, memberId);

		VacationInfoLog log = vacationInfo.toLog();

		assertThat(log.getTotalCount()).isEqualTo(totalCount);
		assertThat(log.getUseCount()).isEqualTo(useCount);
		assertThat(log.getVacationType()).isEqualTo(vacationType);
		assertThat(log.getMemberId()).isEqualTo(memberId);
		assertThat(log.getLogDate()).isNotNull();
		assertThat(log.remainingCount()).isEqualTo(remainingCount);

	}
}