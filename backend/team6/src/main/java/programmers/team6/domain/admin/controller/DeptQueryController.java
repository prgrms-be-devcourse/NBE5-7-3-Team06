package programmers.team6.domain.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.response.DeptDropdownResponse;
import programmers.team6.domain.admin.service.DeptService;

@RestController
@RequestMapping("/depts")
@RequiredArgsConstructor
public class DeptQueryController {

	private final DeptService deptService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<DeptDropdownResponse> getAllDept() {
		return deptService.findAllDept();
	}

}
