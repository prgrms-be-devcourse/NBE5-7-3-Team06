package programmers.team6.domain.vacation.enums;

public enum VacationInfoUpdateResult {
	SUCCESS,
	MISS_VERSION,
	MISS_RULES;

	public boolean isSuccess() {
		return this == SUCCESS;
	}
}
