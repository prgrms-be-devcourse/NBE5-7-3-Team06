package programmers.team6.support

import org.jetbrains.annotations.NotNull
import programmers.team6.domain.admin.entity.Code

object VacationTypeMother {
    fun Annual(): Code {
        return Code("VACATION_TYPE", "01", "연차")
    }

    @NotNull
    fun half(): Code {
        return Code("VACATION_TYPE", "05", "반차")
    }

    @NotNull
    fun reward(): Code {
        return Code("VACATION_TYPE", "02", "포상 휴가")
    }
}
