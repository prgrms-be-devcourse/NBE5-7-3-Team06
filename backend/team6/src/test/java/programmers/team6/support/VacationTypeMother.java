package programmers.team6.support;

import org.jetbrains.annotations.NotNull;

import programmers.team6.domain.admin.entity.Code;

public class VacationTypeMother {

	public static Code Annual() {
		return new Code("VACATION_TYPE", "01", "연차");
	}

	@NotNull
	public static Code half() {
		return new Code("VACATION_TYPE", "05", "반차");
	}

	@NotNull
	public static Code reward() {
		return  new Code("VACATION_TYPE", "02", "포상 휴가");
	}
}
