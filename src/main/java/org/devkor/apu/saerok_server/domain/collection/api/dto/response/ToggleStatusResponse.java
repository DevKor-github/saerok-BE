package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "동의/비동의 토글 결과 응답 DTO")
public record ToggleStatusResponse(
        @Schema(description = "동의 수", example = "8")
        long agreeCount,
        
        @Schema(description = "비동의 수", example = "1")
        long disagreeCount,
        
        @Schema(description = "나의 동의 여부", example = "true")
        boolean isAgreedByMe,
        
        @Schema(description = "나의 비동의 여부", example = "false")
        boolean isDisagreedByMe
) {}
