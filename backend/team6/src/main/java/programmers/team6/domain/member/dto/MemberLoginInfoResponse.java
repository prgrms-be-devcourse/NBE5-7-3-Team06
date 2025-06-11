package programmers.team6.domain.member.dto;

public record MemberLoginInfoResponse(
	Long id,
	String name,
	Long deptId,
	String deptName,
	Long positionId,
	String positionName) {
}
