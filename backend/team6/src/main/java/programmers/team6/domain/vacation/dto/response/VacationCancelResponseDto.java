package programmers.team6.domain.vacation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationCancelResponseDto {
	private Long requestId;
	private boolean success;
	private String message;
}