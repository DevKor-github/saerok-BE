package org.devkor.apu.saerok_server.domain.freeboard.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateFreeBoardPostResponse(
        @Schema(description = "생성된 게시글 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long postId
) {
}
