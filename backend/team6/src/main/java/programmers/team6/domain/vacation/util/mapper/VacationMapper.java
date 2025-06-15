package programmers.team6.domain.vacation.util.mapper;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import programmers.team6.domain.admin.entity.Code;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.dto.request.VacationCreateRequestDto;
import programmers.team6.domain.vacation.dto.response.VacationCreateResponseDto;
import programmers.team6.domain.vacation.dto.response.VacationInfoSelectResponseDto;
import programmers.team6.domain.vacation.dto.response.VacationListResponseDto;
import programmers.team6.domain.vacation.dto.response.VacationUpdateResponseDto;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;

@Component
public class VacationMapper {
	// VacationInfo → VacationInfoSelectResponseDto
	public VacationInfoSelectResponseDto toVacationInfoSelectResponseDto(VacationInfo vacationInfo) {
		return new VacationInfoSelectResponseDto(
			vacationInfo.getTotalCount(),
			vacationInfo.getUseCount()
			);

	}

	// VacationCreateRequestDto → VacationRequest
	public VacationRequest toVacationRequest(VacationCreateRequestDto requestDto, Code vacationType,
		VacationRequestStatus status, Member member) {
		return VacationRequest.builder()
			.from(requestDto.getFrom())
			.to(requestDto.getTo())
			.reason(requestDto.getReason())
			.type(vacationType)
			.status(status)
			.member(member)
			.build();
	}

	// VacationRequest → VacationCreateResponseDto
	public VacationCreateResponseDto toVacationCreateResponseDto(
		VacationRequest vacationRequest,
		String vacationTypeName,
		VacationRequestStatus vacationRequestStatus,
		String approverName) {
		return new VacationCreateResponseDto(
			vacationRequest.getId(),
			vacationRequest.getFrom(),
			vacationRequest.getTo(),
			vacationRequest.getReason(),
			vacationTypeName,
			vacationRequestStatus.name(),
			approverName,
			vacationRequest.getCreatedAt(),
			vacationRequest.getUpdatedAt()
		);
	}

	// VacationRequest → VacationUpdateResponseDto
	// 휴가 요청 수정 후 응답 DTO 생성
	public VacationUpdateResponseDto toVacationUpdateResponseDto(
		VacationRequest vacationRequest,
		String vacationTypeName,
		String approverName) {
		return new VacationUpdateResponseDto(
			vacationRequest.getId(),
			vacationRequest.getFrom(),
			vacationRequest.getTo(),
			vacationRequest.getReason(),
			vacationTypeName,
			vacationRequest.getStatus().name(),
			approverName,
			vacationRequest.getUpdatedAt()
			);
	}

	//
	public VacationListResponseDto toVacationListResponseDto(
		Page<VacationRequest> page,
		List<VacationCreateResponseDto> content) {
		return new VacationListResponseDto(
			content,
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.isFirst(),
			page.isLast()
			);
	}
}