package programmers.team6.domain.admin.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.response.AdminCodeResponse;
import programmers.team6.domain.admin.dto.request.CodeCreateRequest;
import programmers.team6.domain.admin.service.CodeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/code")
public class CodeController {
	private final CodeService codeService;

	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void registerCode(@Valid @RequestBody CodeCreateRequest codeCreateRequest) {
		codeService.createCode(codeCreateRequest);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	AdminCodeResponse retrieveCodePage(@PageableDefault(page = 0, size = 20) Pageable pageable,@RequestParam(name="groupCode",required = false) String groupCode) {
		return codeService.readCodePage(pageable,groupCode);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void modifyCode(@PathVariable("id") Long id, @Valid @RequestBody CodeCreateRequest codeCreateRequest) {
		codeService.updateCode(id, codeCreateRequest);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deleteCode(@PathVariable("id") Long id) {
		codeService.deleteCode(id);
	}
}
