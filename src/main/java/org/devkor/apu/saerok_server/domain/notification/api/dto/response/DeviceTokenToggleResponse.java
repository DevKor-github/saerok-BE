package org.devkor.apu.saerok_server.domain.notification.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "디바이스 푸시 알림 설정 토글 응답 DTO")
public record DeviceTokenToggleResponse(
        @Schema(description = "토글된 디바이스 ID", example = "abc-123-...")
        String deviceId,

        @Schema(description = "토글 후 활성화 상태", example = "false")
        Boolean isActive
) {}
