package programmers.team6.domain.member.entity;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import programmers.team6.domain.member.enums.Role;
import programmers.team6.global.entity.BaseEntity;
import programmers.team6.global.exception.code.BadRequestErrorCode;
import programmers.team6.global.exception.customException.BadRequestException;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

	@Id
	@Column(name = "member_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dept_id")
	private Dept dept;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "position_id")
	private Code position;

	@Column(nullable = false)
	private LocalDateTime joinDate;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role;

	@Setter
	@JoinColumn(name = "member_info_id")
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private MemberInfo memberInfo;

	@Builder
	public Member(String name, Dept dept, Code position, LocalDateTime joinDate, Role role) {
		this.name = name;
		this.dept = dept;
		this.position = position;
		this.joinDate = joinDate;
		this.role = role;
	}

	public void approve() {
		if (this.role != Role.PENDING) {
			throw new BadRequestException(BadRequestErrorCode.BAD_REQUEST_MEMBER_ROLE);
		}
		this.role = Role.USER;
	}

	public void validateDeletableOnReject() {
		if (this.role != Role.PENDING) {
			throw new BadRequestException(BadRequestErrorCode.BAD_REQUEST_MEMBER_ROLE);
		}
	}

	public boolean isHrMember() {
		return this.dept != null && this.dept.isHrDept();
	}

}
