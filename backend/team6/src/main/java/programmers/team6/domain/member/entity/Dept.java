package programmers.team6.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import programmers.team6.global.entity.BaseEntity;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.customException.NotFoundException;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dept extends BaseEntity {
	@Id
	@Column(name = "dept_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String deptName;

	@OneToOne
	@JoinColumn(name = "dept_leader_id")
	private Member deptLeader;

	@Builder(toBuilder = true)
	public Dept(String deptName, Member deptLeader) {
		this.deptName = deptName;
		this.deptLeader = deptLeader;
	}

	public void appointLeader(Member leader) {
		this.deptLeader = leader;
	}

	public Member getDeptLeader() {
		if (this.deptLeader == null) {
			throw new NotFoundException(NotFoundErrorCode.NOT_FOUND_DEPT_LEADER);
		}
		return this.deptLeader;
	}

	public boolean isHrDept() {
		return this.deptName.equals("인사팀");
	}

}
