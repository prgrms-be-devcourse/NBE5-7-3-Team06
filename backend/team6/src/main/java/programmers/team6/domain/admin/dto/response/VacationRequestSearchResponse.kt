package programmers.team6.domain.admin.dto.response

import programmers.team6.domain.vacation.enums.VacationRequestStatus
import java.time.LocalDateTime

data class VacationRequestSearchResponse(
    val id: Long, val type: String, val from: LocalDateTime, val to: LocalDateTime, val applicantName: String,
    val approverNames: List<String>, val deptName: String, val status: VacationRequestStatus
) {
    constructor(
        id: Long, type: String, from: LocalDateTime, to: LocalDateTime, applicantName: String,
        approverNames: String, deptName: String, status: VacationRequestStatus
    ) : this(
        id,
        type,
        from,
        to,
        applicantName,
        approverNames.split(",").toList(),
        deptName,
        status
    )
}
