package org.devkor.apu.saerok_server.domain.admin.role.api.dto.response;

import java.util.List;

public record RoleDetailResponse(
        Long id,
        String code,
        String displayName,
        String description,
        boolean builtin,
        List<PermissionSummaryResponse> permissions
) {
}
