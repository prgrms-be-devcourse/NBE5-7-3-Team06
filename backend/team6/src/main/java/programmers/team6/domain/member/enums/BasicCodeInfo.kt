package programmers.team6.domain.member.enums

enum class BasicCodeInfo(val groupCode: String, val code: String, val codeName: String) {
    ANNUAL("VACATION_TYPE", "01", "연차"),
    REWARD("VACATION_TYPE", "02", "포상 휴가"),
    OFFICIAL("VACATION_TYPE", "03", "공가"),
    CONGRATULATORY("VACATION_TYPE", "04", "경조사 휴가"),
    HALF("VACATION_TYPE", "05", "반차");
    
    companion object {
        @JvmStatic
		fun isIn(groupCode: String, code: String): Boolean {
            for (basicCodeInfo in entries) {
                if (basicCodeInfo.groupCode == groupCode && basicCodeInfo.code == code) {
                    return true
                }
            }
            return false
        }
    }
}

