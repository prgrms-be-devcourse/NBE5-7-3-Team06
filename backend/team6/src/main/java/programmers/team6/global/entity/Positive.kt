package programmers.team6.global.entity

const val MIN_VALUE: Int = 0
const val NEGATIVE_VALUE_ERROR_MESSAGE: String = "해당 값을 음수가 될 수 없습니다."

class Positive(value: Int) {
    private val value: Int

    init {
        this.value = requirePositive(value)
    }

    fun toInt(): Int = value

    fun isEquals(positive: Positive): Boolean = this.value == positive.value

    fun isGraterThan(totalCount: Positive): Boolean = this.value > totalCount.value

    fun isLessThan(totalCount: Positive): Boolean = this.value < totalCount.value

}

private fun requirePositive(value: Int): Int {
    require(value >= MIN_VALUE) { NEGATIVE_VALUE_ERROR_MESSAGE }
    return value
}
