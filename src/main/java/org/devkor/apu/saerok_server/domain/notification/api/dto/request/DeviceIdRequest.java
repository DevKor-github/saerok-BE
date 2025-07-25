package org.devkor.apu.saerok_server.domain.notification.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "디바이스 ID 요청 DTO")
public record DeviceIdRequest(
    @Schema(description = "디바이스 식별자", example = "abc-123-...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "디바이스 ID는 필수입니다.")
    String deviceId
) {
}
