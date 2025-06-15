package programmers.team6.domain.member.enums;

import lombok.Getter;

@Getter
public enum BasicCodeInfo {
	ANNUAL("VACATION_TYPE", "01", "연차"),
	REWARD("VACATION_TYPE", "02", "포상 휴가"),
	OFFICIAL("VACATION_TYPE", "03", "공가"),
	CONGRATULATORY("VACATION_TYPE", "04", "경조사 휴가"),
	HALF("VACATION_TYPE", "05", "반차");
	private final String groupCode;
	private final String code;
	private final String name;

	BasicCodeInfo(String groupCode, String code, String name) {
		this.groupCode = groupCode;
		this.code = code;
		this.name = name;
	}

	public static boolean isIn(String groupCode, String code) {
		for (BasicCodeInfo basicCodeInfo : values()) {
			if (basicCodeInfo.getGroupCode() == groupCode && basicCodeInfo.getCode() == code) {
				return true;
			}
		}
		return false;
	}
}

