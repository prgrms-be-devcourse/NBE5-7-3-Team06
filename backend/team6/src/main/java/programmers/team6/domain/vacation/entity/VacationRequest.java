package programmers.team6.domain.vacation.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import programmers.team6.domain.admin.entity.Code;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;
import programmers.team6.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VacationRequest extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "vacation_request_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Column(name = "from_date", nullable = false)
	private LocalDateTime from;

	@Column(name = "to_date", nullable = false)
	private LocalDateTime to;

	private String reason;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "type_code")
	private Code type;

	@Enumerated(value = EnumType.STRING)
	private VacationRequestStatus status;

	@Version
	private Integer version;

	@Builder
	public VacationRequest(Member member, LocalDateTime from, LocalDateTime to, String reason, Code type,
		VacationRequestStatus status, Integer version) {
		this.member = member;
		this.from = from;
		this.to = to;
		this.reason = reason;
		this.type = type;
		this.status = (status != null) ? status : VacationRequestStatus.IN_PROGRESS;
		this.version = version;
	}

	public void update(Code type, LocalDateTime from, LocalDateTime to, VacationRequestStatus status, String reason) {
		this.type = type;
		this.from = from;
		this.to = to;
		this.status = status;
		this.reason = reason;
	}

	public void updateStatus(VacationRequestStatus vacationRequestStatus) {
		this.status = vacationRequestStatus;
	}

	// UPDATE
	// 현재 요청자가 수정 권한을 가지고 있는지 확인
	public boolean canUpdate(Long memberId) {
		return this.member.getId().equals(memberId) && this.status == VacationRequestStatus.IN_PROGRESS;
	}

	// 수정 권한 검증
	private void validateUpdate(Long memberId) {
		if (!canUpdate(memberId)) {
			// 세부 오류 메시지
			if (!this.member.getId().equals(memberId)) {
				throw new RuntimeException("휴가 신청자만 수정할 수 있습니다.");
			}

			if (this.status != VacationRequestStatus.IN_PROGRESS) {
				throw new IllegalStateException("진행 중인 휴가 요청만 수정할 수 있습니다.");
			}
		}
	}

	// 휴가 수정 권한 검증 후 수정 처리
	public void updateByMember(Long memberId, LocalDateTime from, LocalDateTime to, String reason, Code type) {
		validateUpdate(memberId);
		this.from = from;
		this.to = to;
		this.reason = reason;
		this.type = type;
	}

	// DELETE
	// 현재 요청자가 취소 권한을 가지고 있는지 학인
	public boolean canCancel(Long memberId) {
		return this.member.getId().equals(memberId) && this.status == VacationRequestStatus.IN_PROGRESS;
	}

	// 취소 권한 확인
	private void validateCancel(Long memberId) {
		if (!canCancel(memberId)) {
			// 세부 오류 메시지
			if (!this.member.getId().equals(memberId)) {
				throw new RuntimeException("휴가 신청자만 취소할 수 있습니다.");
			}

			if (this.status == VacationRequestStatus.CANCELED) {
				throw new IllegalStateException("이미 취소된 휴가 신청입니다.");
			}

			throw new IllegalStateException("진행 중인 휴가 요청만 취소할 수 있습니다.");
		}
	}

	// 휴가 신청 취소
	private void changeStatusToCanceled() {
		this.status = VacationRequestStatus.CANCELED;
	}

	// 휴가 취소 권한 검증 후 취소 처리
	public void validateAndCancel(Long memberId) {
		validateCancel(memberId);
		changeStatusToCanceled();
	}

	public void approve() {
		updateStatus(VacationRequestStatus.APPROVED);
	}

	public void reject() {
		updateStatus(VacationRequestStatus.REJECTED);
	}

	public void cancel() {
		updateStatus(VacationRequestStatus.CANCELED);
	}

	public int calcVacationDays() {
		return (int)ChronoUnit.DAYS.between(from.toLocalDate(), to.toLocalDate()) + 1;
	}

	public Long getMemberId() {
		return this.member.getId();
	}

	public String getCode() {
		return this.type.getCode();
	}

	// todo : code 번호 확정 시 변경 고려
	public boolean isHalfDay() {
		return this.type.getName().equals("반차");
	}
}
