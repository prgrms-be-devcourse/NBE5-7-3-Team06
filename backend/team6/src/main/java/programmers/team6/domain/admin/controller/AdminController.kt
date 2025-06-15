package programmers.team6.domain.admin.controller

import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import programmers.team6.domain.admin.dto.request.VacationRequestDetailUpdateRequest
import programmers.team6.domain.admin.dto.response.AdminVacationRequestSearchResponse
import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition
import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition.Companion.bindingApplicantCondition
import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition.Companion.bindingDateRangeCondition
import programmers.team6.domain.admin.dto.response.VacationRequestDetailReadResponse
import programmers.team6.domain.admin.enums.Quarter
import programmers.team6.domain.admin.service.AdminService
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import java.time.LocalDate

@RestController
@RequestMapping("/admin")
class AdminController(
    private val adminService: AdminService
) {

    @GetMapping("/vacation-request")
    @ResponseStatus(HttpStatus.OK)
    fun selectVacationRequests(
        @PageableDefault(page = 0, size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        @RequestParam(value = "start", required = false) startDate: LocalDate?,
        @RequestParam(value = "end", required = false) endDate: LocalDate?,
        @RequestParam(value = "year", required = false) year: Int?,
        @RequestParam(value = "quarter", required = false) quarter: Quarter?,  // 신청자 파라미터

        @RequestParam(value = "name", required = false) name: String?,
        @RequestParam(value = "deptName", required = false) deptName: String?,
        @RequestParam(value = "positionCodeId", required = false) positionCodeId: Long?,
        @RequestParam(value = "vacationTypeCodeId", required = false) vacationTypeCodeId: Long?,  // 휴가 신청 상태

        @RequestParam(value = "vacationRequestStatus", required = false) status: VacationRequestStatus?
    ): AdminVacationRequestSearchResponse {
        return adminService.search(
            pageable, AdminVacationSearchCondition(
                bindingDateRangeCondition(startDate, endDate, year, quarter),
                bindingApplicantCondition(name, deptName, positionCodeId, vacationTypeCodeId),
                status
            )
        )
    }

    @GetMapping("/vacation-request/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun showVacationRequestDetail(@PathVariable id: Long): VacationRequestDetailReadResponse? {
        return adminService.selectVacationRequestDetailById(id)
    }

    @PutMapping("/vacation-request/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateVacationRequestDetail(
        @PathVariable
        id: Long,
        @Valid
        @RequestBody
        vacationRequestDetailUpdateRequest: VacationRequestDetailUpdateRequest
    ) {
        adminService.updateVacationRequestDetailById(id, vacationRequestDetailUpdateRequest)
    }
}
