package org.devkor.apu.saerok_server.domain.mock.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "차단된 사용자 정보")
public record BlockedUserResponse(
        @Schema(description = "사용자 ID", example = "42")
        Long userId,

        @Schema(description = "닉네임", example = "birder123")
        String nickname,

        @Schema(description = "차단 일시", example = "2026-03-18T12:00:00")
        LocalDateTime blockedAt
) {
}
