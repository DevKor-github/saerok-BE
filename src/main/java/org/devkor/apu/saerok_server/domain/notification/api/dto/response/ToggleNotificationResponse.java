package org.devkor.apu.saerok_server.domain.notification.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

/**
 * 알림 설정 토글 응답 DTO
 */
@Schema(description = "알림 설정 토글 응답 DTO")
public record ToggleNotificationResponse(
        @Schema(description = "디바이스 ID", example = "device-123")
        String deviceId,

        @Schema(description = "알림 유형", example = "LIKE")
        NotificationType notificationType,

        @Schema(description = "토글 후 활성화 상태", example = "true")
        Boolean enabled
) {
}
