package programmers.team6.domain.admin.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import programmers.team6.domain.member.dto.CodeReadResponse;

public record AdminCodeResponse(Page<CodeReadResponse> codeReadResponse, List<String> groupCodes) {
}
