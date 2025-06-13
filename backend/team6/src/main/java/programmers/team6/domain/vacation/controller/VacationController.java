package programmers.team6.domain.vacation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import programmers.team6.domain.auth.dto.TokenBody;
import programmers.team6.domain.vacation.dto.response.VacationCancelResponseDto;
import programmers.team6.domain.vacation.dto.request.VacationCreateRequestDto;
import programmers.team6.domain.vacation.dto.response.VacationCreateResponseDto;
import programmers.team6.domain.vacation.dto.response.VacationInfoSelectResponseDto;
import programmers.team6.domain.vacation.dto.response.VacationListResponseDto;
import programmers.team6.domain.vacation.dto.response.VacationRequestCalendarResponse;
import programmers.team6.domain.vacation.dto.request.VacationUpdateRequestDto;
import programmers.team6.domain.vacation.dto.request.VacationUpdateResponseDto;
import programmers.team6.domain.vacation.service.VacationService;

@Slf4j
@RestController
@RequestMapping("/vacations")
@RequiredArgsConstructor
public class VacationController {

	private final VacationService vacationService;

	@GetMapping("/my")
	public ResponseEntity<VacationInfoSelectResponseDto> getMyVacationInfo(
		@AuthenticationPrincipal TokenBody tokenBody) {

		Long memberId = tokenBody.id();

		// 휴가 정보 조회
		VacationInfoSelectResponseDto vacationInfo = vacationService.getMyVacationInfo(memberId);

		return ResponseEntity.ok(vacationInfo);
	}

	// 휴가 신청
	@PostMapping
	public ResponseEntity<VacationCreateResponseDto> requestVacation(
		@Validated @RequestBody VacationCreateRequestDto requestDto,
		@AuthenticationPrincipal TokenBody tokenBody) {

		Long memberId = tokenBody.id();
		VacationCreateResponseDto response = vacationService.requestVacation(memberId, requestDto);
		return ResponseEntity.ok(response);
	}

	// 휴가 신청 리스트 (페이징 조회)
	@GetMapping
	public ResponseEntity<VacationListResponseDto> getVacationRequestList(
		@AuthenticationPrincipal TokenBody tokenBody,
		@RequestParam(defaultValue = "0") int page) {

		Long memberId = tokenBody.id();
		VacationListResponseDto response = vacationService.getVacationRequestList(memberId, page);
		return ResponseEntity.ok(response);
	}

	// 휴가 신청 수정
	@PutMapping("/{requestId}")
	public ResponseEntity<VacationUpdateResponseDto> updateVacationRequest(
		@AuthenticationPrincipal TokenBody tokenBody,
		@PathVariable Long requestId,
		@Validated @RequestBody VacationUpdateRequestDto requestDto) {

		Long memberId = tokenBody.id();
		VacationUpdateResponseDto response = vacationService.updateVacationRequest(memberId, requestId, requestDto);
		return ResponseEntity.ok(response);
	}

	// 대기중인 휴가 신청 취소
	@DeleteMapping("/{requestId}")
	public ResponseEntity<?> cancelVacationRequest(
		@AuthenticationPrincipal TokenBody tokenBody,
		@PathVariable Long requestId) {

		Long memberId = tokenBody.id();

		boolean success = vacationService.cancelVacationRequest(memberId, requestId);

		VacationCancelResponseDto response = VacationCancelResponseDto.builder()
			.requestId(requestId)
			.success(success)
			.message("휴가 신청이 성공적으로 취소되었습니다.")
			.build();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/calendar")
	public ResponseEntity<?> selectVacationCalendar(@RequestParam String yearMonth, @RequestParam Long deptId) {
		
		List<VacationRequestCalendarResponse> vacations
			= vacationService.selectVacationCalendar(yearMonth, deptId);

		return ResponseEntity.ok(vacations);
	}
}
