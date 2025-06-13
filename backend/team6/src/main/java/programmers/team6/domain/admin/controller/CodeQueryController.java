package programmers.team6.domain.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.response.CodeDropdownResponse;
import programmers.team6.domain.admin.service.CodeService;

@RestController
@RequestMapping("/codes")
@RequiredArgsConstructor
public class CodeQueryController {

	private final CodeService codeService;

	@GetMapping("/group/{groupCode}")
	@ResponseStatus(HttpStatus.OK)
	public List<CodeDropdownResponse> getCodesByGroupCode(@PathVariable String groupCode) {
		return codeService.getCodesByGroupCode(groupCode);
	}

}
