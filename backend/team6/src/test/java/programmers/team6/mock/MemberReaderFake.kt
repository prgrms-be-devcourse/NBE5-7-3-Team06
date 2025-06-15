package programmers.team6.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest;
import programmers.team6.domain.admin.support.MemberReader;
import programmers.team6.domain.admin.support.Members;
import programmers.team6.domain.member.entity.Member;

public class MemberReaderFake extends MemberReader {

	private final List<Member> members;

	public MemberReaderFake(Member... members) {
		super(null, null);
		this.members = new ArrayList<>(Arrays.asList(members));
	}

	@Override
	public Members readHasVacationInfoMemberFrom(VacationStatisticsRequest request, Pageable pageable) {
		return new Members(new PageImpl<>(members, pageable, members.size()));
	}
}
