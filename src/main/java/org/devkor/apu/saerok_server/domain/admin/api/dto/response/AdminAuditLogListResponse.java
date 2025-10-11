package org.devkor.apu.saerok_server.domain.admin.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "관리자 감사 로그 목록 응답")
public record AdminAuditLogListResponse(
        List<Item> items
) {
    @Schema(description = "감사 로그 아이템")
    public record Item(
            Long id,
            LocalDateTime createdAt,
            UserMini admin,
            String action,
            String targetType,
            Long targetId,
            Long reportId,
            Map<String, Object> metadata
    ) {}

    @Schema(description = "간단 사용자 정보")
    public record UserMini(
            Long id,
            String nickname
    ) {}
}
