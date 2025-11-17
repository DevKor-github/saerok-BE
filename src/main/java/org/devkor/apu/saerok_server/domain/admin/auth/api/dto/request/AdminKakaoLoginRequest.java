package org.devkor.apu.saerok_server.domain.admin.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 새록 어드민 카카오 로그인 요청.
 * <p>인가 코드 또는 액세스 토큰 중 하나를 전달하면 됩니다.</p>
 */
public record AdminKakaoLoginRequest(
        @Schema(description = "카카오 인가 코드", example = "AuthCode123")
        String authorizationCode,
        @Schema(description = "카카오 액세스 토큰", example = "accessToken123")
        String accessToken
) {
}
