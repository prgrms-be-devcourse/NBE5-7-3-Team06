package programmers.team6.domain.auth.token;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtConfigurationTests {

	@Autowired(required = false)
	JwtConfiguration jwtConfiguration;

	@Test
	@DisplayName("jwtConfiguration test")
	void jwtConfiguration_test() throws Exception {

		assertThat(jwtConfiguration).isNotNull();
		assertThat(jwtConfiguration.secret()).isNotNull();
	}
}