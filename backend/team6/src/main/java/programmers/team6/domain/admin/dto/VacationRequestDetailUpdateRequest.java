package programmers.team6.domain.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;

public record VacationRequestDetailUpdateRequest(@NotNull @Positive Long typeId,
												 @NotNull @FutureOrPresent LocalDateTime from,
												 @NotNull @FutureOrPresent LocalDateTime to,
												 @NotNull VacationRequestStatus vacationRequestStatus,
												 @NotNull String reason,
												 @Valid List<@NotNull String> approvalReason) {

}
