package programmers.team6.domain.vacation.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationUpdateResponseDto {
	private Long requestId;
	private LocalDateTime from;
	private LocalDateTime to;
	private String reason;
	private String vacationType;
	private String approvalStatus;
	private String approverName;
	private LocalDateTime updatedAt;
}