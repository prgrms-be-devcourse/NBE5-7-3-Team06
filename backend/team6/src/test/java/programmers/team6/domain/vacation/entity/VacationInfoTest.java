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
	@DisplayName("총 휴가일수 증가 업데이트")
	void updateTotalCountIncrease() {
		VacationInfo vacationInfo = new VacationInfo(15.0, 5.0, "01", 1L);
		double newTotalCount = 20.0;

		VacationInfoLog log = vacationInfo.updateTotalCount(newTotalCount);

		assertThat(vacationInfo.getTotalCount()).isEqualTo(20.0);
		assertThat(vacationInfo.getUseCount()).isEqualTo(5.0);
		assertThat(log.getTotalCount()).isEqualTo(20.0);
		assertThat(log.getUseCount()).isEqualTo(5.0);
	}
	@Test
	@DisplayName("총 휴가일수 감소 업데이트")
	void updateTotalCountDecrease() {
		VacationInfo vacationInfo = new VacationInfo(15.0, 5.0, "01", 1L);
		double newTotalCount = 12.0;

		VacationInfoLog log = vacationInfo.updateTotalCount(newTotalCount);

		assertThat(vacationInfo.getTotalCount()).isEqualTo(12.0);
		assertThat(vacationInfo.getUseCount()).isEqualTo(5.0);
		assertThat(log.getTotalCount()).isEqualTo(12.0);
		assertThat(log.getUseCount()).isEqualTo(5.0);
	}

	@Test
	@DisplayName("사용일수보다 적은 총일수로 업데이트하면 예외 발생")
	void updateTotalCountLessThanUseCountThrowsException() {
		VacationInfo vacationInfo = new VacationInfo(15.0, 10.0, "01", 1L);
		double newTotalCount = 8.0;

		assertThatThrownBy(() -> vacationInfo.updateTotalCount(newTotalCount))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("잘못된 입력입니다.");

		assertThat(vacationInfo.getTotalCount()).isEqualTo(15.0);
		assertThat(vacationInfo.getUseCount()).isEqualTo(10.0);
	}


	// init() 관련 test
	@Test
	@DisplayName("휴가 정보 초기화 - 총일수 변경 및 사용일수 0")
	void initVacationInfoResetTotalCountAndUseCount() {
		VacationInfo vacationInfo = new VacationInfo(15.0, 10.0, "01", 1L);
		double newTotalCount = 20.0;

		VacationInfoLog log = vacationInfo.init(newTotalCount);

		assertThat(vacationInfo.getTotalCount()).isEqualTo(20.0);
		assertThat(vacationInfo.getUseCount()).isEqualTo(0.0);
		assertThat(log.getTotalCount()).isEqualTo(20.0);
		assertThat(log.getUseCount()).isEqualTo(0.0);

	}
	
	@Test
	@DisplayName("휴가 정보 초기화 - 음수 총일수")
	void initVacationInfoWithNegativeTotalCount() {
	
	    VacationInfo vacationInfo = new VacationInfo(15.0, 10.0, "01", 1L);
		double negativeTotalCount = -5.0;

		assertThatThrownBy(() -> vacationInfo.init(negativeTotalCount))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("잘못된 입력입니다.");
	
	}

	// useVacation() 관련 test
	@Test
	@DisplayName("휴가 사용 시 사용일수 정상 증가")
	void useVacationIncreasesUseCount() {
		VacationInfo vacationInfo = new VacationInfo(15.0, 5.0, "01", 1L);
		double useCount = 3.0;

		VacationInfoLog log = vacationInfo.useVacation(useCount);

		assertThat(vacationInfo.getUseCount()).isEqualTo(8.0);
		assertThat(vacationInfo.getTotalCount()).isEqualTo(15.0);
		assertThat(log.getUseCount()).isEqualTo(8.0);
		assertThat(log.getTotalCount()).isEqualTo(15.0);
		assertThat(log.getVacationType()).isEqualTo("01");
		assertThat(log.getMemberId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("잔여일수보다 많이 사용하려 하면 예외 발생")
	void useVacationExceedsRemainingThrowsException() {
		VacationInfo vacationInfo = new VacationInfo(15.0, 10.0, "01", 1L);
		double useCount = 6.0;

		assertThatThrownBy(() -> vacationInfo.useVacation(useCount))
			.isInstanceOf(BadRequestException.class);

		assertThat(vacationInfo.getUseCount()).isEqualTo(10.0);
		assertThat(vacationInfo.getTotalCount()).isEqualTo(15.0);
	}

	@Test
	@DisplayName("0.5일 반차 사용 처리")
	void useHalfDayVacation() {
		VacationInfo vacationInfo = new VacationInfo(15.0, 5.0, "01", 1L);
		double halfDay = 0.5;

		VacationInfoLog log = vacationInfo.useVacation(halfDay);

		assertThat(vacationInfo.getUseCount()).isEqualTo(5.5);
		assertThat(log.getUseCount()).isEqualTo(5.5);
	}

	@Test
	@DisplayName("정확히 잔여일수만큼 사용하는 경우")
	void useExactRemainingVacation() {
		VacationInfo vacationInfo = new VacationInfo(15, 13, "01", 1L);
		double useCount = 2.0;

		VacationInfoLog log = vacationInfo.useVacation(useCount);

		assertThat(vacationInfo.getUseCount()).isEqualTo(15.0);
		assertThat(log.getUseCount()).isEqualTo(15.0);
	}

	// isSameVersion() 관련 test
	@Test
	@DisplayName("버전 비교 테스트 - 동일한 버전")
	void isSameVersionWhenEqual() {

	    VacationInfo vacationInfo = new VacationInfo(15.0, 10.0, "01", 1L);

		assertThat(vacationInfo.isSameVersion(0)).isTrue();

	}

	@Test
	@DisplayName("버전 비교 테스트 - 다른 버전")
	void isSameVersionWhenDifferent() {

	    VacationInfo vacationInfo = new VacationInfo(15.0, 10.0, "01", 1L);

		assertThat(vacationInfo.isSameVersion(1)).isFalse();
		assertThat(vacationInfo.isSameVersion(-1)).isFalse();

	}

	// canUseVacation() 관련 test
	@Test
	@DisplayName("휴가 사용 검증 - 사용 가능한 경우")
	void canUseVacationWhenSufficient() {
		VacationInfo vacationInfo = new VacationInfo(15.0, 5.0, "01", 1L);

		assertThat(vacationInfo.canUseVacation(10.0)).isTrue();
		assertThat(vacationInfo.canUseVacation(5.0)).isTrue();
		assertThat(vacationInfo.canUseVacation(0.5)).isTrue();
		assertThat(vacationInfo.canUseVacation(0.0)).isTrue();
	}

	@Test
	@DisplayName("휴가 사용 검증 - 사용 불가능한 경우")
	void canUseVacationWhenInsufficient() {

	    VacationInfo vacationInfo = new VacationInfo(15.0, 5.0, "01", 1L);

		assertThat(vacationInfo.canUseVacation(11.0)).isFalse();

	}

	@Test
	@DisplayName("휴가 사용 검증 - 이미 모든 휴가를 사용한 경우")
	void canUseVacationWhenFullUsed() {

	    VacationInfo vacationInfo = new VacationInfo(15.0, 15.0, "01", 1L);

		assertThat(vacationInfo.canUseVacation(0.0)).isTrue();
		assertThat(vacationInfo.canUseVacation(0.5)).isFalse();
		assertThat(vacationInfo.canUseVacation(1.0)).isFalse();

	}

	// toLog() 관련 test
	@Test
	@DisplayName("VacationInfoLog 생성 확인")
	void toLogCreate() {

	    VacationInfo vacationInfo = new VacationInfo(15.0, 5.0, "01", 1L);

		VacationInfoLog log = vacationInfo.toLog();

		assertThat(log.getTotalCount()).isEqualTo(15.0);
		assertThat(log.getUseCount()).isEqualTo(5.0);
		assertThat(log.getVacationType()).isEqualTo("01");
		assertThat(log.getMemberId()).isEqualTo(1L);
		assertThat(log.getLogDate()).isNotNull();
		assertThat(log.remainingCount()).isEqualTo(10.0);

	}
}