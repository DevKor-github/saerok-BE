package org.devkor.apu.saerok_server.domain.freeboard.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateFreeBoardPostCommentResponse(
        @Schema(description = "생성된 댓글 ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        Long commentId
) {
}
