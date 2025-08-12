package org.devkor.apu.saerok_server.domain.notification.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 설정 조회 응답 DTO")
public record NotificationSettingsResponse(
        @Schema(description = "디바이스 ID", example = "device-123")
        String deviceId,

        @Schema(description = "좋아요 알림 활성화 여부", example = "true")
        Boolean LIKE,

        @Schema(description = "댓글 알림 활성화 여부", example = "true")
        Boolean COMMENT,

        @Schema(description = "동정 제안 알림 활성화 여부", example = "true")
        Boolean BIRD_ID_SUGGESTION,

        @Schema(description = "시스템 알림 활성화 여부", example = "true")
        Boolean SYSTEM
) {
}
