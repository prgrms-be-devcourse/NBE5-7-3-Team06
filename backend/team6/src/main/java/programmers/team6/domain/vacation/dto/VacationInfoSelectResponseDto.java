package programmers.team6.domain.vacation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VacationInfoSelectResponseDto {
	private double totalCount;
	private double useCount;

	public double getRemainCount() {
		return totalCount - useCount;
	}
}
