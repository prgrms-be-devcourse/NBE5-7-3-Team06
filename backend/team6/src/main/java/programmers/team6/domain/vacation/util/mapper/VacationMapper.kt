package programmers.team6.domain.vacation.util.mapper

import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.dto.request.VacationCreateRequestDto
import programmers.team6.domain.vacation.dto.response.VacationCreateResponseDto
import programmers.team6.domain.vacation.dto.response.VacationInfoSelectResponseDto
import programmers.team6.domain.vacation.dto.response.VacationListResponseDto
import programmers.team6.domain.vacation.dto.response.VacationUpdateResponseDto
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.VacationRequestStatus

@Component
class VacationMapper {
    // VacationInfo → VacationInfoSelectResponseDto
    fun toVacationInfoSelectResponseDto(vacationInfo: VacationInfo): VacationInfoSelectResponseDto {
        return VacationInfoSelectResponseDto(
            vacationInfo.totalCount,
            vacationInfo.useCount
        )
    }

    // VacationCreateRequestDto → VacationRequest
    fun toVacationRequest(
        requestDto: VacationCreateRequestDto,
        vacationType: Code,
        status: VacationRequestStatus,
        member: Member
    ): VacationRequest {
        return VacationRequest(
                member,
        		requestDto.from,
        		requestDto.to,
            	requestDto.reason,
            	vacationType,
            	status
                );

    }

    // VacationRequest → VacationCreateResponseDto
    fun toVacationCreateResponseDto(
        vacationRequest: VacationRequest,
        vacationTypeName: String,
        vacationRequestStatus: VacationRequestStatus,
        approverName: String
    ): VacationCreateResponseDto {
        return VacationCreateResponseDto(
            vacationRequest.id,
            vacationRequest.from,
            vacationRequest.to,
            vacationRequest.reason,
            vacationTypeName,
            vacationRequestStatus.name,
            approverName,
            vacationRequest.createdAt,
            vacationRequest.updatedAt
        )
    }

    // VacationRequest → VacationUpdateResponseDto
    // 휴가 요청 수정 후 응답 DTO 생성
    fun toVacationUpdateResponseDto(
        vacationRequest: VacationRequest,
        vacationTypeName: String,
        approverName: String
    ): VacationUpdateResponseDto {
        return VacationUpdateResponseDto(
            vacationRequest.id,
            vacationRequest.from,
            vacationRequest.to,
            vacationRequest.reason,
            vacationTypeName,
            vacationRequest.status.name,
            approverName,
            vacationRequest.updatedAt
        )
    }

    //
    fun toVacationListResponseDto(
        page: Page<VacationRequest>,
        content: List<VacationCreateResponseDto>
    ): VacationListResponseDto {
        return VacationListResponseDto(
            content,
            page.number,
            page.size,
            page.totalElements,
            page.totalPages,
            page.isFirst,
            page.isLast
        )
    }
}