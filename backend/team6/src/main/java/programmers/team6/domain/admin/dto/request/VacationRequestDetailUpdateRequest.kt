package programmers.team6.domain.admin.dto.request

import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import java.time.LocalDateTime

data class VacationRequestDetailUpdateRequest(
	@field:NotNull @field:Positive
	val typeId:  Long,
	@field:NotNull @field:FutureOrPresent
	val from:  LocalDateTime,
	@field:NotNull @field:FutureOrPresent
	val to:  LocalDateTime,
	@field:NotNull
	val vacationRequestStatus:  VacationRequestStatus,
	@field:NotNull
	val reason:  String,
	@field:NotNull
	val approvalReason:  List<String>
)
