package programmers.team6.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CodeCreateRequest(@NotBlank String groupCode, @NotBlank String code, @NotBlank String name) {
}
