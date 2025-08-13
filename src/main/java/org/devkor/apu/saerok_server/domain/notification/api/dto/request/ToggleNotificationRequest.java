package org.devkor.apu.saerok_server.domain.notification.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;

/** 알림 설정 토글 요청 (subject/action 기반) */
public record ToggleNotificationRequest(
        @Schema(description = "디바이스 식별자", example = "device-123")
        String deviceId,

        @Schema(description = "알림 주제", example = "COLLECTION")
        NotificationSubject subject,

        @Schema(description = "알림 액션(그룹 토글이면 null)", example = "LIKE", nullable = true)
        NotificationAction action
) {}
