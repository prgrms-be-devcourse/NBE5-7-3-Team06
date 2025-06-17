package programmers.team6.global.entity

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class PositiveTest {
    @Test
    fun 양수이면_정상생성() {
        Assertions.assertThatCode { Positive(0) }.doesNotThrowAnyException()
    }

    @Test
    fun 음수이면_예외발생() {
        Assertions.assertThatIllegalArgumentException().isThrownBy { Positive(-1) }
    }
}