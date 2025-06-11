package programmers.team6.domain.admin.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.VacationInfoUpdateTotalCountRequestsList;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.repository.MemberSearchRepository;
import programmers.team6.domain.vacation.dto.MemberVacationInfoSelectResponse;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.repository.VacationInfoRepository;
import programmers.team6.domain.vacation.service.VacationInfoService;
import programmers.team6.domain.vacation.util.mapper.VacationInfoMapper;
import programmers.team6.global.paging.PagingConfig;

@RestController
@RequestMapping("/admin/vacations/infos")
@RequiredArgsConstructor
public class AdminVacationInfoController {

	private final VacationInfoService vacationInfoService;
	private final MemberSearchRepository memberSearchRepository;
	private final VacationInfoRepository vacationInfoRepository;
	private final VacationInfoMapper vacationInfoMapper;

	@GetMapping
	@ResponseStatus(value = HttpStatus.OK)
	public Page<MemberVacationInfoSelectResponse> selectVacationInfos(@PagingConfig(sort = "id") Pageable pageable,
		@RequestParam(required = false) Long deptId, @RequestParam(required = false) String name) {
		Page<Member> members = memberSearchRepository.searchFrom(name, deptId, pageable);
		List<VacationInfo> vacationInfos = vacationInfoRepository.findByMemberIdIn(toIds(members));
		return vacationInfoMapper.toMemberVacationInfoSelectResponsePageFrom(members, vacationInfos);
	}

	private List<Long> toIds(Page<Member> members) {
		return members.map(Member::getId).toList();
	}

	@PatchMapping
	@ResponseStatus(value = HttpStatus.OK)
	public void updateTotalCount(
		@Validated
		@RequestBody VacationInfoUpdateTotalCountRequestsList request) {
		vacationInfoService.updateFrom(request);
	}
}
