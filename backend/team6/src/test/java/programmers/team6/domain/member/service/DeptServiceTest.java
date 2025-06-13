package programmers.team6.domain.member.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import programmers.team6.domain.member.repository.DeptRepository;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.customException.NotFoundException;

@ExtendWith(MockitoExtension.class)
class DeptServiceTest {
	@InjectMocks
	DeptService deptService;
	@Mock
	DeptRepository deptRepository;

	@Test
	void should_throwNotFoundException_when_givenNotExistDeptName() {
		// when & then
		Assertions.assertThatThrownBy(() -> deptService.findByDeptName("EMPTY"))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(NotFoundErrorCode.NOT_FOUND_DEPT.getMessage());
	}

}