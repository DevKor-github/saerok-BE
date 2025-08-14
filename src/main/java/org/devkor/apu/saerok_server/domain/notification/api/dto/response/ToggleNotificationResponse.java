package org.devkor.apu.saerok_server.domain.notification.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

public record ToggleNotificationResponse(
        @Schema(description = "디바이스 식별자") String deviceId,
        @Schema(description = "알림 식별자") NotificationType type,
        @Schema(description = "활성화 여부") boolean enabled
) {}
