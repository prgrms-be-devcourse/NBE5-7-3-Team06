package programmers.team6.domain.vacation.enums

enum class VacationInfoUpdateResult {
    SUCCESS,
    MISS_VERSION,
    MISS_RULES;

    val isSuccess: Boolean
        get() = this == SUCCESS
}
