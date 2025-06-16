package programmers.team6.domain.admin.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import programmers.team6.domain.admin.repository.DeptRepository
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.NotFoundException

internal class DeptServiceTest {
    private var deptRepository = mockk<DeptRepository>()

    private var deptService = DeptService(deptRepository)

    @Test
    fun should_throwNotFoundException_when_givenNotExistDeptName() {
        // given
        every { deptRepository.findByDeptName("EMPTY") } returns null

        // when & then
        Assertions.assertThatThrownBy { deptService.findByDeptName("EMPTY") }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage(NotFoundErrorCode.NOT_FOUND_DEPT.message)
    }
}