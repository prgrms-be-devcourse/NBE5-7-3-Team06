package programmers.team6.domain.vacation.enums;

import java.util.Arrays;
import java.util.Optional;

public enum VacationCode {
	ANNUAL("VACATION_TYPE", "01", "연차"),
	REWARD("VACATION_TYPE", "02", "포상 휴가"),
	OFFICIAL("VACATION_TYPE", "03", "공가"),
	CONGRATULATORY("VACATION_TYPE", "04", "경조사 휴가");

	private final String groupCode;
	private final String code;
	private final String description;

	VacationCode(String groupCode, String code, String description) {
		this.groupCode = groupCode;
		this.code = code;
		this.description = description;
	}

	public static Optional<VacationCode> findByCode(String type) {
		return Arrays.stream(values()).filter(vacationCode -> vacationCode.getCode().equals(type)).findFirst();
	}

	public String getGroupCode() {
		return groupCode;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
}
