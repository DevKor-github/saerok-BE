package org.devkor.apu.saerok_server.domain.admin.role.api.dto.response;

public record RoleSummaryResponse(
        Long id,
        String code,
        String displayName,
        String description,
        boolean builtin
) {
}
