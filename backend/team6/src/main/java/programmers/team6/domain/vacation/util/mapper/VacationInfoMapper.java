package programmers.team6.domain.vacation.util.mapper;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.dto.MemberVacationInfoSelectResponse;
import programmers.team6.domain.vacation.dto.VacationInfoSelectResponse;
import programmers.team6.domain.vacation.entity.VacationInfo;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VacationInfoMapper {

	public Page<MemberVacationInfoSelectResponse> toMemberVacationInfoSelectResponsePageFrom(Page<Member> members,
		List<VacationInfo> vacationInfos) {
		return members.map(
			member -> this.toMemberVacationSelectResponse(member, findVacationInfos(vacationInfos, member.getId())));
	}

	private MemberVacationInfoSelectResponse toMemberVacationSelectResponse(Member member,
		List<VacationInfo> vacationInfos) {
		List<VacationInfoSelectResponse> responses = vacationInfos.stream()
			.map(this::toVacationInfoSelectResponseFrom)
			.toList();
		return new MemberVacationInfoSelectResponse(member.getId(), member.getName(), responses);
	}

	private VacationInfoSelectResponse toVacationInfoSelectResponseFrom(VacationInfo info) {
		return new VacationInfoSelectResponse(info.getVacationId(),
			info.getTotalCount(),
			info.getVacationType(),
			info.getVersion());
	}

	private List<VacationInfo> findVacationInfos(List<VacationInfo> infos, Long memberId) {
		return infos.stream().filter(info -> info.getMemberId().equals(memberId)).toList();
	}
}
