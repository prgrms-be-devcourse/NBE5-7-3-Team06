package programmers.team6.domain.admin.dto;

public record MemberApprovalResponse(
	Long memberId,
	String name,
	String positionName,
	String deptName,
	String birth,
	String email
) {
}
