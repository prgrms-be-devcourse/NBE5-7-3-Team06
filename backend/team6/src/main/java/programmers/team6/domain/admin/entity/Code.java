package programmers.team6.domain.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import programmers.team6.global.entity.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "code",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"group_code", "code"})
	}
)
public class Code extends BaseEntity {

	@Id
	@Column(name = "code_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "group_code", nullable = false)
	private String groupCode;

	@Column(nullable = false)
	private String code;

	@Column(nullable = false)
	private String name;

	@Builder
	public Code(String groupCode, String code, String name) {
		this.groupCode = groupCode;
		this.code = code;
		this.name = name;
	}

	public void updateCode(String groupCode, String code, String name) {
		this.groupCode = groupCode;
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getGroupCode() {
		return groupCode;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
