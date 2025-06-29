package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좋아요 상태 응답 DTO")
public record LikeStatusResponse(
        @Schema(description = "좋아요 여부", example = "true")
        boolean isLiked
) {}
