package programmers.team6.domain.vacation.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

class VacationUpdateRequestDto(
    @field:NotNull(message = "시작일은 필수입니다.")
    val from: LocalDateTime,

    @field:NotNull(message = "종료일은 필수입니다.")
    val to: LocalDateTime,

    @field:NotBlank(message = "사유는 필수입니다.")
    val reason: String,

    @field:NotBlank(message = "휴가 유형은 필수입니다.")
    val vacationType: String
)