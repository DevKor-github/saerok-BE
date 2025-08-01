package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "동의 상태 응답 DTO")
public record AgreeStatusResponse(
        @Schema(description = "동의 여부", example = "true")
        boolean isAgreed
) {}
