package programmers.team6.domain.admin.service

import lombok.RequiredArgsConstructor
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import programmers.team6.domain.admin.dto.request.CodeCreateRequest
import programmers.team6.domain.admin.dto.response.AdminCodeResponse
import programmers.team6.domain.admin.dto.response.CodeDropdownResponse
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.admin.utils.mapper.CodeMapper
import programmers.team6.domain.member.enums.BasicCodeInfo.Companion.isIn
import programmers.team6.global.exception.code.BadRequestErrorCode
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.BadRequestException
import programmers.team6.global.exception.customException.NotFoundException

@Service
@Transactional
@RequiredArgsConstructor
class CodeService(
    private val codeRepository: CodeRepository
) {

    fun createCode(codeCreateRequest: CodeCreateRequest) {
        try {
            codeRepository.save(CodeMapper.toCode(codeCreateRequest))
        } catch (e: DataIntegrityViolationException) {
            throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_DUPLICATE_CODE)
        }
    }

    // TODO - codeRepository.findGroupCodes()가 순서대로 받지않아 뒤죽박죽
    @Transactional(readOnly = true)
    fun readCodePage(pageable: Pageable, groupCode: String?): AdminCodeResponse {
        return AdminCodeResponse(codeRepository.findCodePage(pageable, groupCode), codeRepository.findGroupCodes())
    }

    fun updateCode(id: Long, codeCreateRequest: CodeCreateRequest) {
        val code = codeRepository.findCodeById(id) ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_CODE)

        code.updateCode(codeCreateRequest.groupCode, codeCreateRequest.code, codeCreateRequest.name)
    }

    /**
     * 삭제하려는 code가 필수 CODE인 경우 삭제하지 못하도록 수정
     * @param id
     */
    fun deleteCode(id: Long) {
        val deletedTarget = codeRepository.findCodeById(id) ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_CODE)

        if (isIn(deletedTarget.groupCode, deletedTarget.code)) {
            return
        }
        codeRepository.delete(deletedTarget)
    }

    fun getCodesByGroupCode(groupCode: String): MutableList<CodeDropdownResponse> {
        return codeRepository.findByGroupCode(groupCode)
    }
}
