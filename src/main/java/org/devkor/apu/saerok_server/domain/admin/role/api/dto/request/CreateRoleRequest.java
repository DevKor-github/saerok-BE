package org.devkor.apu.saerok_server.domain.admin.role.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequest(
        @NotBlank(message = "역할 코드를 입력해 주세요") String code,
        @NotBlank(message = "표시 이름을 입력해 주세요") String displayName,
        @NotBlank(message = "설명을 입력해 주세요") String description
) {
}
