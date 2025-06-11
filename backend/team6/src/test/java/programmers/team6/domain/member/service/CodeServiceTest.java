package programmers.team6.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.member.dto.CodeCreateRequest;
import programmers.team6.domain.member.entity.Code;
import programmers.team6.domain.member.repository.CodeRepository;
import programmers.team6.global.exception.code.BadRequestErrorCode;
import programmers.team6.global.exception.customException.BadRequestException;
import programmers.team6.global.exception.customException.NotFoundException;

@DataJpaTest
@RequiredArgsConstructor
@Import(CodeService.class)
class CodeServiceTest {
	private final CodeRepository codeRepository;
	private final CodeService codeService;

	@BeforeEach
	void setUp() {
		codeRepository.save(
			Code.builder().groupCode("TEST_GROUP_CODE").code("TEST_CODE").name("TEST_NAME").build()
		);
	}

	@AfterEach
	void tearDown() {
		codeRepository.deleteAll();
	}

	@Test
	void should_createCode_when_givenValidCodeCreateRequest() {
		// given
		CodeCreateRequest codeCreateRequest = new CodeCreateRequest("TEST_GROUP_CODE", "TEST_CODE", "TEST_NAME");

		// when
		when(codeRepository.save(any(Code.class))).thenReturn(null);

		// then
		assertThatCode(() -> codeService.createCode(codeCreateRequest)).doesNotThrowAnyException();
	}

	@Test
	void should_throwBadRequestException_when_givenDuplicatedGroupCodeAndCode() {
		// given
		CodeCreateRequest codeCreateRequest = new CodeCreateRequest("DUPLICATED", "DUPLICATED", "TEST_NAME");

		// when
		when(codeRepository.save(any(Code.class))).thenThrow(DataIntegrityViolationException.class);

		// then
		assertThatThrownBy(() -> codeService.createCode(codeCreateRequest))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(BadRequestErrorCode.BAD_REQUEST_DUPLICATE_CODE.getMessage());
	}

	@Test
	void should_updateCode_when_givenValidCodeRequest() {
		// given
		Code code = Code.builder().build();
		CodeCreateRequest codeCreateRequest = new CodeCreateRequest("TEST_GROUP_CODE", "TEST_CODE", "TEST_NAME");

		// when
		when(codeRepository.findById(0L)).thenReturn(Optional.of(code));
		codeService.updateCode(0L, codeCreateRequest);

		// then
		assertThat(code.getGroupCode()).isEqualTo(codeCreateRequest.groupCode());
		assertThat(code.getCode()).isEqualTo(codeCreateRequest.code());
		assertThat(code.getName()).isEqualTo(codeCreateRequest.name());
	}

	@Test
	void should_throwNotFoundException_when_givenInvalidCodeId() {
		// given
		Code code = Code.builder().build();
		CodeCreateRequest codeCreateRequest = new CodeCreateRequest("TEST_GROUP_CODE", "TEST_CODE", "TEST_NAME");

		// when
		when(codeRepository.findById(0L)).thenThrow(NotFoundException.class);


		codeService.updateCode(0L, codeCreateRequest);
	}

}