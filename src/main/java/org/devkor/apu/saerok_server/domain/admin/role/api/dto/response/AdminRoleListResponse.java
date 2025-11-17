package org.devkor.apu.saerok_server.domain.admin.role.api.dto.response;

import java.util.List;

public record AdminRoleListResponse(
        List<RoleDetailResponse> roles
) {
}
