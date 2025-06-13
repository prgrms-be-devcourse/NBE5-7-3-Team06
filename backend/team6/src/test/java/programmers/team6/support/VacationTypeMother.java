package programmers.team6.support;

import programmers.team6.domain.admin.entity.Code;

public class VacationTypeMother {

	public static Code Annual() {
		return new Code("VACATION_TYPE", "01", "연차");
	}
}
