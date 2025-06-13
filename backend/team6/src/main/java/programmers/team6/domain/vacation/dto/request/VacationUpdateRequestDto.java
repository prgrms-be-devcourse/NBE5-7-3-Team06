package programmers.team6.domain.vacation.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationUpdateRequestDto {
	@NotNull(message = "시작일은 필수입니다.")
	private LocalDateTime from;

	@NotNull(message = "종료일은 필수입니다.")
	private LocalDateTime to;

	@NotBlank(message = "사유는 필수입니다.")
	private String reason;

	@NotBlank(message = "휴가 유형은 필수입니다.")
	private String vacationType;
}
