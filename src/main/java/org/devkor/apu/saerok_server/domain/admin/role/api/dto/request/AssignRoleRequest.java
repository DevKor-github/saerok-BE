package org.devkor.apu.saerok_server.domain.admin.role.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AssignRoleRequest(
        @NotBlank(message = "부여할 역할 코드를 입력해 주세요") String roleCode
) {
}
