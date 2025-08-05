package org.devkor.apu.saerok_server.domain.notification.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로컬 환경 더미 디바이스 토큰 생성 응답")
public record LocalDeviceTokenResponse(

        @Schema(description = "생성된 더미 디바이스 ID", example = "dummy_device_99999")
        String deviceId,

        @Schema(description = "생성된 더미 FCM 토큰", example = "dummy_fcm_token_99999")
        String fcmToken,

        @Schema(description = "등록된 사용자 ID", example = "99999")
        Long userId
) {
}
