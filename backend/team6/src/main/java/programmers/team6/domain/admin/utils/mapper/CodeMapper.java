package programmers.team6.domain.admin.utils.mapper;

import lombok.experimental.UtilityClass;
import programmers.team6.domain.admin.dto.request.CodeCreateRequest;
import programmers.team6.domain.admin.entity.Code;
import programmers.team6.domain.member.enums.BasicCodeInfo;

@UtilityClass
public class CodeMapper {
	public static Code toCode(CodeCreateRequest codeCreateRequest) {
		return Code.builder()
			.groupCode(codeCreateRequest.groupCode())
			.code(codeCreateRequest.code())
			.name(codeCreateRequest.name())
			.build();
	}

	public static Code toCode(BasicCodeInfo basicCodeInfo) {
		return Code.builder()
			.groupCode(basicCodeInfo.getGroupCode())
			.code(basicCodeInfo.getCode())
			.name(basicCodeInfo.getName())
			.build();
	}

}
