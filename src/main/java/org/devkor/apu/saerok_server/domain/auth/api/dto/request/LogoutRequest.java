package org.devkor.apu.saerok_server.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DevicePlatform;

@Schema(description = "로그아웃 요청 DTO")
public record LogoutRequest(
        @Schema(description = "쿠키 대신 JSON Body로 전달하는 Refresh Token (모바일 앱용)")
        String refreshTokenJson,

        @Schema(description = "현재 로그아웃하는 디바이스 식별자")
        String deviceId,

        @Schema(description = "현재 로그아웃하는 디바이스 플랫폼", example = "IOS")
        DevicePlatform platform
) {
}
