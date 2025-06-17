package programmers.team6.support

import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.member.enums.GroupCode
import programmers.team6.domain.vacation.enums.VacationCode

enum class TestVacationType {
    ANNUAL {
        override fun toCode(): Code {
            return Code(GroupCode.VACATION_TYPE.code, VacationCode.ANNUAL.code, VacationCode.ANNUAL.name)
        }
    },
    REWARD {
        override fun toCode(): Code {
            return Code(GroupCode.VACATION_TYPE.code, VacationCode.REWARD.code, VacationCode.REWARD.name)
        }
    },
    OFFICIAL {
        override fun toCode(): Code {
            return Code(GroupCode.VACATION_TYPE.code, VacationCode.OFFICIAL.code, VacationCode.OFFICIAL.name)
        }
    },
    CONGRATULATORY {
        override fun toCode(): Code {
            return Code(
                GroupCode.VACATION_TYPE.code,
                VacationCode.CONGRATULATORY.code,
                VacationCode.CONGRATULATORY.name
            )
        }
    },
    HALF {
        override fun toCode(): Code {
            return Code(GroupCode.VACATION_TYPE.code, "05", "반차")
        }
    };

    abstract fun toCode(): Code
}
