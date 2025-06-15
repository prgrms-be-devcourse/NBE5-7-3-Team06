package programmers.team6.domain.admin.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import programmers.team6.domain.admin.dto.response.AdminCodeResponse;
import programmers.team6.domain.admin.dto.request.CodeCreateRequest;
import programmers.team6.domain.admin.entity.Code;
import programmers.team6.domain.member.enums.BasicCodeInfo;
import programmers.team6.domain.admin.repository.CodeRepository;
import programmers.team6.global.exception.code.BadRequestErrorCode;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.customException.BadRequestException;
import programmers.team6.global.exception.customException.NotFoundException;

/**
 * 서비스 테스트이지만 Code 엔티티 특성상 CRUD 서비스밖에 없기 때문에 테스트 과정에서 DB 체크가 필요했음
 * 리포지토리에서 테스트하기에는 로직이 필요하고 통합 테스트를 하기에 과투자같아서 위와같이 DataJpaTest 진행
 * @author gunwoong
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CodeService.class)
class CodeServiceIntegrationTest {
	@Autowired
	private CodeRepository codeRepository;
	@Autowired
	private CodeService codeService;

	@Test
	@DisplayName("저장할 code가 유니크한 (groupCode, code)와 name를 갖고잇을 경우, code 저장")
	void should_createCode_when_givenValidCodeCreateRequest() {
		String groupCode = UUID.randomUUID().toString();
		String code = UUID.randomUUID().toString();
		String name = UUID.randomUUID().toString();
		CodeCreateRequest codeCreateRequest = new CodeCreateRequest(groupCode, code, name);

		// when
		codeService.createCode(codeCreateRequest);

		// then
		List<Code> codes = codeRepository.findAll();
		assertThat(codes).hasSize(1)
			.first()
			.extracting(Code::getGroupCode, Code::getCode, Code::getName)
			.containsExactly(groupCode, code, name);
	}

	@Test
	@DisplayName("저장할 code가 중복된 (groupCode, code)일 경우, BadRequestErrorCode 발생")
	void should_throwBadRequestErrorCode_when_givenGroupCodeAndCodeAreDuplicated() {
		// given
		String groupCode = "TEST_GROUP_CODE";
		String code = "TEST_CODE";
		String name = "TEST_NAME";
		codeRepository.save(new Code(groupCode, code, name));

		// when & then
		assertThatThrownBy(() -> codeService.createCode(new CodeCreateRequest(groupCode, code, name))).isInstanceOf(
			BadRequestException.class).hasMessage(BadRequestErrorCode.BAD_REQUEST_DUPLICATE_CODE.getMessage());
	}

	@Test
	@DisplayName("null이 아닌 groupCode가 주어질경우, 해당 groupCode에 해당하는 code들 조회")
	void should_readCodePage_when_givenNotNullGroupCode() {
		// given
		int anotherCodeCnt = 2;
		int targetCodeCnt = 3;
		String targetGroupCode = "TEST_TARGET_GROUP_CODE";

		// when
		genCode(targetGroupCode, "TEST_TARGET_NAME", targetCodeCnt);
		genCode("TEST_ANOTHER_GROUP_CODE", "TEST_ANOTHER_NAME", anotherCodeCnt);

		// then
		AdminCodeResponse response = codeService.readCodePage(PageRequest.of(0, targetCodeCnt + anotherCodeCnt + 1),
			targetGroupCode);
		assertThat(response.getCodeReadResponse().getTotalElements()).isEqualTo(targetCodeCnt);
		assertThat(response.getGroupCodes()).hasSize(2);
	}

	@Test
	@DisplayName("null인 groupCode가 주어질경우, 전체 groupCode에 해당하는 code들 조회")
	void should_readTotalCodePage_when_givenNullGroupCode() {
		// given
		int anotherCodeCnt = 4;
		int targetCodeCnt = 0;
		String targetGroupCode = null;

		// when
		genCode(targetGroupCode, "TEST_TARGET_NAME", targetCodeCnt);
		genCode("TEST_ANOTHER_GROUP_CODE", "TEST_ANOTHER_NAME", anotherCodeCnt);

		// then
		AdminCodeResponse response = codeService.readCodePage(PageRequest.of(0, targetCodeCnt + anotherCodeCnt + 1),
			targetGroupCode);
		assertThat(targetGroupCode).isNull();
		assertThat(targetCodeCnt).isEqualTo(0);
		assertThat(response.getCodeReadResponse().getTotalElements()).isEqualTo(targetCodeCnt + anotherCodeCnt);
		assertThat(response.getGroupCodes()).hasSize(1);
	}

	@Test
	@DisplayName("update 혹은 delete 과정에서 존재하지 않는 code id 제공시, NotFoundException 발생")
	void should_throwNotFoundException_when_givenInvalidCodeId() {
		assertThatThrownBy(() -> {
			codeService.updateCode(0L, new CodeCreateRequest("", "", ""));
			codeService.deleteCode(0L);
		}).isInstanceOf(NotFoundException.class).hasMessage(NotFoundErrorCode.NOT_FOUND_CODE.getMessage());
	}

	@Test
	@DisplayName("delete 과정에서 존재하는 code id 제공시, 정상적으로 hard delete")
	void should_deleteCode_when_givenCodeId() {
		// given
		Code code = codeRepository.save(new Code("TEST_GROUP_CODE", "TEST_CODE", "TEST_NAME"));

		// when
		codeService.deleteCode(code.getId());

		// then
		Optional<Code> result = codeRepository.findById(code.getId());
		assertThat(result).isEmpty();
	}

	// 테스트를 위해 기본 분류 코드를 따로 추가하지않고 지정한 기존 분류 코드를 생성함으로서 테스트 진행하였음
	@Test
	@DisplayName("delete 과정에서 기본 분류코드 code id 제공시, hard delete 무시")
	void should_ignoreDelete_when_givenBasicCodeId() {
		// given
		BasicCodeInfo basicCodeInfo = BasicCodeInfo.ANNUAL;
		Code code = codeRepository.save(
			new Code(basicCodeInfo.getGroupCode(), basicCodeInfo.getCode(), basicCodeInfo.getCodeName()));

		// when
		codeService.deleteCode(code.getId());

		// then
		Optional<Code> result = codeRepository.findById(code.getId());
		assertThat(result).isPresent();
	}

	private void genCode(String groupCode, String prefixName, int cnt) {
		if (groupCode == null) {
			return;
		}
		for (int i = 0; i < cnt; i++) {
			codeRepository.save(new Code(groupCode, String.format("%02d", i), String.format("%s%d", prefixName, i)));
		}
	}

}