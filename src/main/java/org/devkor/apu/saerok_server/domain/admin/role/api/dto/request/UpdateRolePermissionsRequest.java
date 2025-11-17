package org.devkor.apu.saerok_server.domain.admin.role.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.devkor.apu.saerok_server.global.security.permission.PermissionKey;

public record UpdateRolePermissionsRequest(
        @NotNull(message = "권한 목록을 입력해 주세요") List<PermissionKey> permissions
) {
}
