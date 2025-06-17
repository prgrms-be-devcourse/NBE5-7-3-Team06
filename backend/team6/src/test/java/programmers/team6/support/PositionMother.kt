package programmers.team6.support

import programmers.team6.domain.admin.entity.Code

object PositionMother {
    fun employee(): Code {
        return Code("POSITION", "01", "사원")
    }
}
