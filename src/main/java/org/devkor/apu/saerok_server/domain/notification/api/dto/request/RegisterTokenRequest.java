package org.devkor.apu.saerok_server.domain.notification.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "디바이스 토큰 등록/갱신 요청 DTO")
public record RegisterTokenRequest(
    @Schema(description = "디바이스 고유 식별자", example = "device-123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "디바이스 ID는 필수입니다")
    String deviceId,

    @Schema(description = "FCM 토큰", example = "fGKdE...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "토큰은 필수입니다")
    String token
) {
}
