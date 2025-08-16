package org.devkor.apu.saerok_server.domain.notification.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

/** 알림 설정 토글 요청 (type 기반) */
public record ToggleNotificationRequest(
        @Schema(description = "디바이스 식별자", example = "device-123")
        String deviceId,

        @Schema(description = "알림 식별자", example = "LIKED_ON_COLLECTION")
        NotificationType type
) {}
