package org.devkor.apu.saerok_server.domain.freeboard.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateFreeBoardPostCommentResponse(
        @Schema(description = "수정된 댓글 ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        Long commentId,
        @Schema(description = "수정된 댓글 내용", example = "수정된 댓글입니다", requiredMode = Schema.RequiredMode.REQUIRED)
        String content
) {
}
