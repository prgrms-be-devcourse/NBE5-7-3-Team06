package programmers.team6.domain.vacation.dto.response;

import java.util.List;

public record MemberVacationInfoSelectResponse(Long id, String name, List<VacationInfoSelectResponse> vacationInfos) {
}
