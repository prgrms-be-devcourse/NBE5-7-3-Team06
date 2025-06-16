package programmers.team6.domain.vacation.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import programmers.team6.domain.auth.dto.TokenBody
import programmers.team6.domain.vacation.dto.request.VacationCreateRequestDto
import programmers.team6.domain.vacation.dto.request.VacationUpdateRequestDto
import programmers.team6.domain.vacation.dto.response.*
import programmers.team6.domain.vacation.service.VacationService

@RestController
@RequestMapping("/vacations")
class VacationController(
    private val vacationService: VacationService
) {

    @GetMapping("/my")
    fun getMyVacationInfo(
        @AuthenticationPrincipal tokenBody: TokenBody
    ): ResponseEntity<VacationInfoSelectResponseDto> {
        val memberId = tokenBody.id

        // 휴가 정보 조회
        val vacationInfo = vacationService.getMyVacationInfo(memberId)

        return ResponseEntity.ok(vacationInfo)
    }

    // 휴가 신청
    @PostMapping
    fun requestVacation(
        @Validated @RequestBody requestDto: VacationCreateRequestDto,
        @AuthenticationPrincipal tokenBody: TokenBody
    ): ResponseEntity<VacationCreateResponseDto> {
        val memberId = tokenBody.id
        val response = vacationService.requestVacation(memberId, requestDto)
        return ResponseEntity.ok(response)
    }

    // 휴가 신청 리스트 (페이징 조회)
    @GetMapping
    fun getVacationRequestList(
        @AuthenticationPrincipal tokenBody: TokenBody,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<VacationListResponseDto> {
        val memberId = tokenBody.id
        val response = vacationService.getVacationRequestList(memberId, page)
        return ResponseEntity.ok(response)
    }

    // 휴가 신청 수정
    @PutMapping("/{requestId}")
    fun updateVacationRequest(
        @AuthenticationPrincipal tokenBody: TokenBody,
        @PathVariable requestId: Long,
        @Validated @RequestBody requestDto: VacationUpdateRequestDto
    ): ResponseEntity<VacationUpdateResponseDto> {
        val memberId = tokenBody.id
        val response = vacationService.updateVacationRequest(memberId, requestId, requestDto)
        return ResponseEntity.ok(response)
    }

    // 대기중인 휴가 신청 취소
    @DeleteMapping("/{requestId}")
    fun cancelVacationRequest(
        @AuthenticationPrincipal tokenBody: TokenBody,
        @PathVariable requestId: Long
    ): ResponseEntity<*> {
        val memberId = tokenBody.id

        val success = vacationService.cancelVacationRequest(memberId, requestId)

        val response = VacationCancelResponseDto(
            requestId,
            success,
            "휴가 신청이 성공적으로 취소되었습니다."
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/calendar")
    fun selectVacationCalendar(
        @RequestParam yearMonth: String,
        @RequestParam deptId: Long?
    ): ResponseEntity<*> {
        val vacations = vacationService.selectVacationCalendar(yearMonth, deptId)

        return ResponseEntity.ok(vacations)
    }
}
