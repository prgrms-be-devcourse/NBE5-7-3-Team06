package programmers.team6.domain.admin.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import java.time.LocalDateTime

data class VacationRequestDetailUpdateRequest(
	val typeId: @NotNull @Positive Long,
	val from: @NotNull @FutureOrPresent LocalDateTime,
	val to: @NotNull @FutureOrPresent LocalDateTime,
	val vacationRequestStatus: @NotNull VacationRequestStatus,
	val reason: @NotNull String,
	val approvalReason: @Valid MutableList<String>
)
