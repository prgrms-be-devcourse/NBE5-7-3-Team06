package programmers.team6.global.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PositiveTest {

	@Test
	void 양수이면_정상생성(){
		assertThatCode(()->new Positive(0)).doesNotThrowAnyException();
	}

	@Test
	void 음수이면_예외발생(){
		assertThatIllegalArgumentException().isThrownBy(()->new Positive(-1));
	}
}