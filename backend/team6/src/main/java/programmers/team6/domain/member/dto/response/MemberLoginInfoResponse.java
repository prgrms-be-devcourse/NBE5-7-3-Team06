package programmers.team6.domain.member.dto.response;

public record MemberLoginInfoResponse(
	Long id,
	String name,
	Long deptId,
	String deptName,
	Long positionId,
	String positionName) {
}
