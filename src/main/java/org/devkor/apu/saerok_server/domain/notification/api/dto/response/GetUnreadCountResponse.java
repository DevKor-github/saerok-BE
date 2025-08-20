package org.devkor.apu.saerok_server.domain.notification.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "읽지 않은 알림 개수 조회 응답")
public record GetUnreadCountResponse(
    @Schema(description = "읽지 않은 알림 개수")
    Long unreadCount
) {}
