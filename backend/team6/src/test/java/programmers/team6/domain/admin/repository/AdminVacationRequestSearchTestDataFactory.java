package programmers.team6.domain.admin.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import programmers.team6.domain.admin.entity.Code;
import programmers.team6.domain.admin.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.enums.Role;
import programmers.team6.domain.vacation.entity.ApprovalStep;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.enums.ApprovalStatus;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;

public class AdminVacationRequestSearchTestDataFactory {
	public static ApprovalStep genTestApprovalStep(VacationRequest vacationRequest, int step, String reason) {
		return new ApprovalStep(null, vacationRequest, null, step, reason);
	}

	public static Code genTestCode(String groupCode, String code, String name) {
		return new Code(groupCode, code, name);
	}

	public static List<Code> genTestCodeList(String groupCode, int cnt, String prefixName) {
		List<Code> result = new ArrayList<>();
		for (int i = 0; i <= cnt; i++) {
			result.add(genTestCode(groupCode, String.format("%02d", i), String.format("%s%d", prefixName, i)));
		}
		return result;
	}

	public static Dept genTestDept(String deptName) {
		return Dept.builder()
			.deptName(deptName)
			.build();
	}

	public static List<Dept> genTestDeptList(int cnt, String prefixDeptName) {
		List<Dept> result = new ArrayList<>();
		for (int i = 0; i < cnt; i++) {
			result.add(genTestDept(String.format("%s%d", prefixDeptName, i)));
		}
		return result;
	}

	public static Member genTestMember(String name, Dept dept, Code positionCode) {
		return Member.builder()
			.name(name)
			.dept(dept)
			.position(positionCode)
			.role(Role.USER)
			.joinDate(LocalDateTime.now())
			.build();
	}

	public static List<Member> genTestMemberList(int cnt, char startName, List<Dept> depts, List<Code> positionCodes) {
		List<Member> result = new ArrayList<>();
		for (int i = 0; i < cnt; i++) {
			result.add(
				genTestMember(String.format("%s%d", (char)(startName + i), i), depts.get(i), positionCodes.get(i)));
		}
		return result;
	}

	public static List<Member> genTestMemberList(int cnt, String prefixName, List<Dept> depts, Code positionCode) {
		List<Member> result = new ArrayList<>();
		for (int i = 0; i < cnt; i++) {
			result.add(genTestMember(String.format("%s%d", prefixName, i), depts.get(i), positionCode));
		}
		return result;
	}

	public static VacationRequest genVacationRequest(Member member, LocalDateTime start, LocalDateTime end,
		String reason, Code type, VacationRequestStatus status) {
		return VacationRequest.builder()
			.member(member)
			.from(start)
			.to(end)
			.reason(reason)
			.type(type)
			.status(status)
			.build();
	}

	public static ApprovalStep genApprovalStep(int step, ApprovalStatus approvalStatus, Member member,
		VacationRequest vacationRequest) {
		return ApprovalStep.builder()
			.step(step)
			.approvalStatus(approvalStatus)
			.member(member)
			.vacationRequest(vacationRequest)
			.build();
	}
}
