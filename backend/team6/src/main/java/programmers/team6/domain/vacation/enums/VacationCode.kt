package programmers.team6.domain.vacation.enums

import java.util.*

enum class VacationCode(val groupCode: String,val code:String, val description: String) {
    ANNUAL("VACATION_TYPE", "01", "연차"),
    REWARD("VACATION_TYPE", "02", "포상 휴가"),
    OFFICIAL("VACATION_TYPE", "03", "공가"),
    CONGRATULATORY("VACATION_TYPE", "04", "경조사 휴가");

    companion object {

        @JvmStatic
        fun findByCode(type: String): VacationCode? {
            return VacationCode.entries.firstOrNull{ it.code == type }
        }
    }
}
