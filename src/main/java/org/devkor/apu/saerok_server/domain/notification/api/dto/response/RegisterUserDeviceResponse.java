package org.devkor.apu.saerok_server.domain.notification.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "디바이스 토큰 등록/갱신 응답 DTO")
public record RegisterUserDeviceResponse(
        @Schema(description = "등록/갱신된 디바이스 ID", example = "abc-123-...")
        String deviceId,

        @Schema(description = "등록/갱신 성공 여부", example = "true")
        Boolean success
) {}
