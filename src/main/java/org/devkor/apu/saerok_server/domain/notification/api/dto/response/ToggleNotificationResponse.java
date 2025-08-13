package org.devkor.apu.saerok_server.domain.notification.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;

public record ToggleNotificationResponse(
        @Schema(description = "디바이스 식별자")
        String deviceId,
        @Schema(description = "주제")
        NotificationSubject subject,
        @Schema(description = "액션(그룹이면 null)")
        NotificationAction action,
        @Schema(description = "토글 결과 on/off")
        boolean enabled
) {}
