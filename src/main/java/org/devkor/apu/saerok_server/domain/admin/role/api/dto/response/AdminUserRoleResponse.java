package org.devkor.apu.saerok_server.domain.admin.role.api.dto.response;

import java.util.List;

public record AdminUserRoleResponse(
        Long userId,
        String nickname,
        String email,
        boolean superAdmin,
        List<RoleSummaryResponse> roles,
        List<PermissionSummaryResponse> permissions
) {
}
