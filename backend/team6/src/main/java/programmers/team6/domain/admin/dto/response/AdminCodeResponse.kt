package programmers.team6.domain.admin.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record AdminCodeResponse(Page<CodeReadResponse> codeReadResponse, List<String> groupCodes) {
}
