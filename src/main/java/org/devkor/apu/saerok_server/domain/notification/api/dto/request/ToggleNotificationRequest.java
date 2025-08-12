package org.devkor.apu.saerok_server.domain.notification.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

@Schema(description = "알림 설정 토글 요청 DTO")
public record ToggleNotificationRequest(
        @Schema(description = "디바이스 ID", example = "device-123", requiredMode = Schema.RequiredMode.REQUIRED)
        String deviceId,

        @Schema(description = "알림 유형(LIKE, COMMENT, BIRD_ID_SUGGESTION, SYSTEM 중 하나)", example = "LIKE", requiredMode = Schema.RequiredMode.REQUIRED)
        NotificationType notificationType
) {
}
