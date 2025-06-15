package programmers.team6.domain.admin.utils.mapper

import programmers.team6.domain.admin.dto.request.CodeCreateRequest
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.member.enums.BasicCodeInfo

object CodeMapper {
    fun toCode(codeCreateRequest: CodeCreateRequest): Code {
        return Code(
            groupCode = codeCreateRequest.groupCode,
            code = codeCreateRequest.code,
            name = codeCreateRequest.name
        )
    }

    fun toCode(basicCodeInfo: BasicCodeInfo): Code {
        return Code(groupCode = basicCodeInfo.groupCode, code = basicCodeInfo.code, name = basicCodeInfo.name)
    }
}
