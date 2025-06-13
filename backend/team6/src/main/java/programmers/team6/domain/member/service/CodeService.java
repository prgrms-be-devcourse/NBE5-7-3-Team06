package programmers.team6.domain.member.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.response.AdminCodeResponse;
import programmers.team6.domain.member.dto.CodeCreateRequest;
import programmers.team6.domain.member.dto.CodeDropdownResponse;
import programmers.team6.domain.member.entity.Code;
import programmers.team6.domain.member.enums.BasicCodeInfo;
import programmers.team6.domain.member.repository.CodeRepository;
import programmers.team6.domain.member.util.mapper.CodeMapper;
import programmers.team6.global.exception.code.BadRequestErrorCode;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.customException.BadRequestException;
import programmers.team6.global.exception.customException.NotFoundException;

@Service
@Transactional
@RequiredArgsConstructor
public class CodeService {
	private final CodeRepository codeRepository;

	public void createCode(CodeCreateRequest codeCreateRequest) {
		try {
			codeRepository.save(CodeMapper.toCode(codeCreateRequest));
		} catch (DataIntegrityViolationException e) {
			throw new BadRequestException(BadRequestErrorCode.BAD_REQUEST_DUPLICATE_CODE);
		}
	}

	// TODO - codeRepository.findGroupCodes()가 순서대로 받지않아 뒤죽박죽
	@Transactional(readOnly = true)
	public AdminCodeResponse readCodePage(Pageable pageable,String groupCode) {
		return new AdminCodeResponse(codeRepository.findCodePage(pageable,groupCode), codeRepository.findGroupCodes());
	}

	public void updateCode(Long id, CodeCreateRequest codeCreateRequest) {
		Code code = codeRepository.findById(id)
			.orElseThrow(() -> new NotFoundException(NotFoundErrorCode.NOT_FOUND_CODE));

		code.updateCode(codeCreateRequest.groupCode(), codeCreateRequest.code(), codeCreateRequest.name());
	}

	/**
	 * 삭제하려는 code가 필수 CODE인 경우 삭제하지 못하도록 수정
	 * @param id
	 */
	public void deleteCode(Long id) {
		Code deletedTarget = codeRepository.findById(id)
			.orElseThrow(() -> new NotFoundException(NotFoundErrorCode.NOT_FOUND_CODE));
		if (BasicCodeInfo.isIn(deletedTarget.getGroupCode(), deletedTarget.getCode())) {
			return;
		}
		codeRepository.delete(deletedTarget);
	}

	public List<CodeDropdownResponse> getCodesByGroupCode(String groupCode) {

		return codeRepository.findByGroupCode(groupCode);
	}

}
