package org.devkor.apu.saerok_server.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record RefreshRequest(

        @Schema(description = "Json Body로 전달된 리프레시 토큰")
        String refreshTokenJson
) {
}
