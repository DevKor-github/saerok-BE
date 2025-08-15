// ===== ./src/main/java/org/devkor/apu/saerok_server/domain/notification/api/dto/response/NotificationSettingsResponse.java =====
package org.devkor.apu.saerok_server.domain.notification.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

import java.util.List;

@Schema(description = "디바이스별 알림 설정 응답 (type 단일 축)")
public record NotificationSettingsResponse(
        @Schema(description = "디바이스 식별자", example = "device-123")
        String deviceId,

        @Schema(description = "설정 목록")
        List<Item> items
) {
    @Schema(name = "NotificationSettingsResponse.Item")
    public record Item(
            @Schema(description = "알림 식별자") NotificationType type,
            @Schema(description = "활성화 여부") boolean enabled
    ) {}
}
