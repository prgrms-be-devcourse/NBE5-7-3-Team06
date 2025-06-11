package programmers.team6.domain.vacation.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

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

}