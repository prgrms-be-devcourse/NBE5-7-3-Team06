package programmers.team6.domain.vacation.dto.response

@JvmRecord
data class MemberVacationInfoSelectResponse(
    val id: Long,
    val name: String,
    val vacationInfos: List<VacationInfoSelectResponse>
)
