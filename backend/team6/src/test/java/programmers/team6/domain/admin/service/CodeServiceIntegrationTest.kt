package programmers.team6.domain.admin.service

import org.assertj.core.api.Assertions
import org.assertj.core.api.ThrowableAssert.ThrowingCallable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import programmers.team6.domain.admin.dto.request.CodeCreateRequest
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchTestDataFactory
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.member.enums.BasicCodeInfo
import programmers.team6.global.exception.code.BadRequestErrorCode
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.BadRequestException
import programmers.team6.global.exception.customException.NotFoundException
import java.util.*

/**
 * 서비스 테스트이지만 Code 엔티티 특성상 CRUD 서비스밖에 없기 때문에 테스트 과정에서 DB 체크가 필요했음
 * 리포지토리에서 테스트하기에는 로직이 필요하고 통합 테스트를 하기에 과투자같아서 위와같이 DataJpaTest 진행
 * @author gunwoong
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CodeService::class)
internal class CodeServiceIntegrationTest {
    @Autowired
    lateinit var codeRepository: CodeRepository

    @Autowired
    lateinit var codeService: CodeService

    @Test
    @DisplayName("저장할 code가 유니크한 (groupCode, code)와 name를 갖고잇을 경우, code 저장")
    fun should_createCode_when_givenValidCodeCreateRequest() {
        // given
        val groupCode = UUID.randomUUID().toString()
        val code = UUID.randomUUID().toString()
        val name = UUID.randomUUID().toString()
        val codeCreateRequest = CodeCreateRequest(groupCode, code, name)

        // when
        codeService.createCode(codeCreateRequest)

        // then
        val codes = codeRepository.findAll()
        Assertions.assertThat(codes).hasSize(1)
            .first()
            .extracting(Code::groupCode, Code::code, Code::name)
            .containsExactly(groupCode, code, name)
    }

    @Test
    @DisplayName("저장할 code가 중복된 (groupCode, code)일 경우, BadRequestErrorCode 발생")
    fun should_throwBadRequestErrorCode_when_givenGroupCodeAndCodeAreDuplicated() {
        // given
        val groupCode = "TEST_GROUP_CODE"
        val code = "TEST_CODE"
        val name = "TEST_NAME"
        codeRepository.save(Code(groupCode, code, name))

        // when & then
        Assertions.assertThatThrownBy( {
            codeService.createCode(
                CodeCreateRequest(
                    groupCode,
                    code,
                    name
                )
            )
        }).isInstanceOf(
            BadRequestException::class.java
        ).hasMessage(BadRequestErrorCode.BAD_REQUEST_DUPLICATE_CODE.getMessage())
    }

    @Test
    @DisplayName("null이 아닌 groupCode가 주어질경우, 해당 groupCode에 해당하는 code들 조회")
    fun should_readCodePage_when_givenNotNullGroupCode() {
        // given
        val anotherCodeCnt = 2
        val targetCodeCnt = 3
        val targetGroupCode = "TEST_TARGET_GROUP_CODE"

        // when
        genCode(targetGroupCode, "TEST_TARGET_NAME", targetCodeCnt)
        genCode("TEST_ANOTHER_GROUP_CODE", "TEST_ANOTHER_NAME", anotherCodeCnt)

        // then
        val response = codeService.readCodePage(
            PageRequest.of(0, targetCodeCnt + anotherCodeCnt + 1),
            targetGroupCode
        )
        Assertions.assertThat(response.codeReadResponse.getTotalElements()).isEqualTo(targetCodeCnt.toLong())
        Assertions.assertThat(response.groupCodes).hasSize(2)
    }

    @Test
    @DisplayName("null인 groupCode가 주어질경우, 전체 groupCode에 해당하는 code들 조회")
    fun should_readTotalCodePage_when_givenNullGroupCode() {
        // given
        val anotherCodeCnt = 4
        val targetCodeCnt = 0
        val targetGroupCode: String? = null

        // when
        genCode("TEST_ANOTHER_GROUP_CODE", "TEST_ANOTHER_NAME", anotherCodeCnt)

        // then
        val response = codeService.readCodePage(
            PageRequest.of(0, targetCodeCnt + anotherCodeCnt + 1),
            targetGroupCode
        )
        Assertions.assertThat(targetGroupCode).isNull()
        Assertions.assertThat(targetCodeCnt).isEqualTo(0)
        Assertions.assertThat(response.codeReadResponse.getTotalElements())
            .isEqualTo((targetCodeCnt + anotherCodeCnt).toLong())
        Assertions.assertThat(response.groupCodes).hasSize(1)
    }

    @Test
    @DisplayName("update 혹은 delete 과정에서 존재하지 않는 code id 제공시, NotFoundException 발생")
    fun should_throwNotFoundException_when_givenInvalidCodeId() {
        Assertions.assertThatThrownBy( {
            codeService.updateCode(0L, CodeCreateRequest("", "", ""))
            codeService.deleteCode(0L)
        }).isInstanceOf(NotFoundException::class.java).hasMessage(NotFoundErrorCode.NOT_FOUND_CODE.getMessage())
    }

    @Test
    @DisplayName("delete 과정에서 존재하는 code id 제공시, 정상적으로 hard delete")
    fun should_deleteCode_when_givenCodeId() {
        // given
        val code = codeRepository.save<Code>(Code("TEST_GROUP_CODE", "TEST_CODE", "TEST_NAME"))

        // when
        codeService.deleteCode(code.id!!)

        // then
        val result = codeRepository.findById(code.id)
        Assertions.assertThat(result).isEmpty()
    }

    // 테스트를 위해 기본 분류 코드를 따로 추가하지않고 지정한 기존 분류 코드를 생성함으로서 테스트 진행하였음
    @Test
    @DisplayName("delete 과정에서 기본 분류코드 code id 제공시, hard delete 무시")
    fun should_ignoreDelete_when_givenBasicCodeId() {
        // given
        val basicCodeInfo = BasicCodeInfo.ANNUAL
        val code = codeRepository.save<Code>(
            Code(basicCodeInfo.groupCode, basicCodeInfo.code, basicCodeInfo.codeName)
        )

        // when
        codeService.deleteCode(code.id!!)

        // then
        val result = codeRepository.findById(code.id)
        Assertions.assertThat(result).isPresent()
    }

    private fun genCode(groupCode: String, prefixName: String, cnt: Int) {
        for (i in 0..<cnt) {
            codeRepository.save(
                Code(
                    groupCode,
                    String.format("%02d", i),
                    String.format("%s%d", prefixName, i)
                )
            )
        }
    }
}