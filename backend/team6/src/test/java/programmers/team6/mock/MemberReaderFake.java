package programmers.team6.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import programmers.team6.domain.admin.dto.VacationStatisticsRequest;
import programmers.team6.domain.admin.service.MemberReader;
import programmers.team6.domain.admin.service.Members;
import programmers.team6.domain.member.entity.Member;

public class MemberReaderFake extends MemberReader {

	private final List<Member> members;

	public MemberReaderFake(Member... members) {
		super(null, null);
		this.members = new ArrayList<>(Arrays.asList(members));
	}

	@Override
	public Members readHasVacationInfoMemberFrom(VacationStatisticsRequest request, Pageable pageable) {
		List<Member> list = members.stream()
			.filter(member -> isSameName(request.name(), member))
			.filter(member -> isSameDept(request.deptId(), member))
			.toList();
		return new Members(new PageImpl<>(list, pageable, list.size()));
	}

	private boolean isSameName(String name, Member member) {
		if (name == null) {
			return true;
		}
		return member.getName().contains(name);
	}

	private boolean isSameDept(Long deptId, Member member) {
		if (deptId == null) {
			return true;
		}
		if (member.getDept() == null) {
			return false;
		}
		return deptId.equals(member.getDept().getId());
	}
}
