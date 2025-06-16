package programmers.team6.support

import programmers.team6.domain.admin.entity.Code

object VacationTypeMother {
    fun Annual(): Code {
        return Code("VACATION_TYPE", "01", "연차")
    }
}
