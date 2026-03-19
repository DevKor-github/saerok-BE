package org.devkor.apu.saerok_server.domain.freeboard.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateFreeBoardPostResponse(
        @Schema(description = "수정된 게시글 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long postId,
        @Schema(description = "수정된 내용", example = "수정된 내용입니다", requiredMode = Schema.RequiredMode.REQUIRED)
        String content
) {
}
