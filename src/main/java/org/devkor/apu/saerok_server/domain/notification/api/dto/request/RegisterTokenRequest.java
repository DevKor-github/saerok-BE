package org.devkor.apu.saerok_server.domain.notification.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 디바이스 등록/갱신 요청 DTO")
public record RegisterTokenRequest(
    @Schema(description = "디바이스 고유 식별자", example = "device-123", requiredMode = Schema.RequiredMode.REQUIRED)
    String deviceId,

    @Schema(description = "푸시 알림 수신용 토큰 (FCM 토큰)", example = "fGKdE...", requiredMode = Schema.RequiredMode.REQUIRED)
    String token
) {
}
