package programmers.team6.domain.member.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.member.dto.DeptDropdownResponse;
import programmers.team6.domain.member.entity.Dept;
import programmers.team6.domain.member.repository.DeptRepository;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.customException.NotFoundException;

@Service
@RequiredArgsConstructor
public class DeptService {

	private final DeptRepository deptRepository;

	public List<DeptDropdownResponse> findAllDept() {
		return deptRepository.findAllDept();
	}

	public Dept findByDeptName(String deptName) {
		return deptRepository.findByDeptName(deptName)
			.orElseThrow(() -> new NotFoundException(NotFoundErrorCode.NOT_FOUND_DEPT));
	}

}
