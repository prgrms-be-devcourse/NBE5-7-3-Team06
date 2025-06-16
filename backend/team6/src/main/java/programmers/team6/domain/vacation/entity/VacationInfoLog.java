package programmers.team6.domain.vacation.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VacationInfoLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private double totalCount;

	private double useCount;

	private String vacationType;

	private Long memberId;

	private LocalDateTime logDate;

	public VacationInfoLog(double totalCount, double useCount, String vacationType, Long memberId) {
		this.totalCount = totalCount;
		this.useCount = useCount;
		this.vacationType = vacationType;
		this.memberId = memberId;
		this.logDate = LocalDateTime.now();
	}

	public VacationInfoLog(double totalCount, double useCount, String vacationType, Long memberId,
		LocalDateTime logDate) {
		this.totalCount = totalCount;
		this.useCount = useCount;
		this.vacationType = vacationType;
		this.memberId = memberId;
		this.logDate = logDate;
	}

	public static VacationInfoLog from(VacationInfo vacationInfo) {
		return new VacationInfoLog(vacationInfo.getTotalCount(), vacationInfo.getUseCount(),
			vacationInfo.getVacationType(), vacationInfo.getMemberId());
	}

	public boolean isSameMemberId(Long memberId) {
		return this.memberId.equals(memberId);
	}

	public double remainingCount() {
		return totalCount - useCount;
	}

	public Long getId() {
		return id;
	}

	public double getTotalCount() {
		return totalCount;
	}

	public double getUseCount() {
		return useCount;
	}

	public String getVacationType() {
		return vacationType;
	}

	public Long getMemberId() {
		return memberId;
	}

	public LocalDateTime getLogDate() {
		return logDate;
	}
}
