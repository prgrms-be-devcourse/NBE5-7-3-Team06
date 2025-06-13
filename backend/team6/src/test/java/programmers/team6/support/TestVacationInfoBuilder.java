package programmers.team6.support;

import programmers.team6.domain.vacation.entity.VacationInfo;

public class TestVacationInfoBuilder {

	private double totalCount;
	private String vacationType;
	private Long memberId;

	public TestVacationInfoBuilder totalCount(double totalCount) {
		this.totalCount = totalCount;
		return this;
	}

	public TestVacationInfoBuilder vacationType(String vacationType) {
		this.vacationType = vacationType;
		return this;
	}

	public TestVacationInfoBuilder memberId(Long memberId) {
		this.memberId = memberId;
		return this;
	}

	public VacationInfo build() {
		return new VacationInfo(totalCount, vacationType, memberId);
	}
}
