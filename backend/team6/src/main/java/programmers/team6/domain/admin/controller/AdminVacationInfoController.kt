package programmers.team6.domain.admin.controller

import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import programmers.team6.domain.admin.dto.request.VacationInfoUpdateTotalCountRequestsList
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.repository.MemberSearchRepository
import programmers.team6.domain.vacation.dto.response.MemberVacationInfoSelectResponse
import programmers.team6.domain.vacation.repository.VacationInfoRepository
import programmers.team6.domain.vacation.service.VacationInfoService
import programmers.team6.domain.vacation.util.mapper.VacationInfoMapper
import programmers.team6.global.paging.PagingConfig

@RestController
@RequestMapping("/admin/vacations/infos")
@RequiredArgsConstructor
class AdminVacationInfoController(
    private val vacationInfoService: VacationInfoService,
    private val memberSearchRepository: MemberSearchRepository,
    private val vacationInfoRepository: VacationInfoRepository,
) {


    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    fun selectVacationInfos(
        @PagingConfig(sort = ["id"]) pageable: Pageable,
        @RequestParam(required = false) deptId: Long?, @RequestParam(required = false) name: String?
    ): Page<MemberVacationInfoSelectResponse> {
        val members = memberSearchRepository.searchFrom(name, deptId, pageable)
        val vacationInfos = vacationInfoRepository.findByMemberIdIn(toIds(members))
        return VacationInfoMapper.toMemberVacationInfoSelectResponsePageFrom(members, vacationInfos)
    }

    private fun toIds(members: Page<Member>): List<Long> {
        return members.mapNotNull(Member::id).toList()
    }

    @PatchMapping
    @ResponseStatus(value = HttpStatus.OK)
    fun updateTotalCount(
        @Validated @RequestBody request: VacationInfoUpdateTotalCountRequestsList
    ) {
        vacationInfoService.updateFrom(request)
    }
}
