package programmers.team6.domain.vacation.entity;

import org.springframework.lang.CheckReturnValue;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import programmers.team6.global.entity.BaseEntity;
import programmers.team6.global.exception.code.BadRequestErrorCode;
import programmers.team6.global.exception.customException.BadRequestException;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class VacationInfo extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int vacationId;

	private double totalCount;

	private double useCount;

	private String vacationType;

	private Long memberId;

	@Version
	private int version;

	public VacationInfo(double totalCount, String vacationType, Long memberId) {
		this(totalCount, 0, vacationType, memberId);
	}

	public VacationInfo(double totalCount, double useCount, String vacationType, Long memberId) {
		this.totalCount = totalCount;
		this.useCount = useCount;
		this.vacationType = vacationType;
		this.memberId = memberId;
		this.version = 0;
	}

	@CheckReturnValue
	public VacationInfoLog updateTotalCount(double totalCount) {
		return update(totalCount, this.useCount);
	}

	@CheckReturnValue
	public VacationInfoLog init(double totalCount) {
		return update(totalCount, 0);
	}

	@CheckReturnValue
	public VacationInfoLog useVacation(double count) {
		return update(this.totalCount, this.useCount + count);
	}

	public boolean isSameVersion(Integer version) {
		return this.version == version;
	}

	public boolean canUseVacation(double count) {
		return this.useCount + count <= this.totalCount;
	}

	@CheckReturnValue
	private VacationInfoLog update(double totalCount, double useCount) {
		if (useCount > totalCount) {
			throw new BadRequestException(BadRequestErrorCode.BAD_REQUEST_INVALID_INPUT);
		}
		this.totalCount = totalCount;
		this.useCount = useCount;
		return toLog();
	}

	public VacationInfoLog toLog() {
		return VacationInfoLog.from(this);
	}
}
