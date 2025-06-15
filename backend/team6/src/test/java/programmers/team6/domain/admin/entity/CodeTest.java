package programmers.team6.domain.admin.entity;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CodeTest {
	@Test
	@DisplayName("변경할 데이터가 유니크한 (groupCode, code)와 name일 경우, 성공적으로 code를 update")
	void should_successUpdate_when_givenValidData() {
		// given
		Code code = new Code("", "", "");
		String updatedGroupCode = UUID.randomUUID().toString();
		String updatedCode = UUID.randomUUID().toString();
		String updatedName = UUID.randomUUID().toString();

		// when
		code.updateCode(updatedGroupCode, updatedCode, updatedName);

		// then
		assertThat(code).extracting("groupCode", "code", "name")
			.containsExactly(updatedGroupCode, updatedCode, updatedName);
	}
}