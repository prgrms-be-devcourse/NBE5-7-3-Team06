package programmers.team6.domain.member.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GroupCode {
	VACATION_TYPE("VACATION_TYPE");

	private final String code;
}
