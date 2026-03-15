package org.devkor.apu.saerok_server.domain.freeboard.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GetFreeBoardPostCommentCountResponse(
        @Schema(description = "활성 댓글 수 (ACTIVE 상태인 원댓글 + 대댓글)", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
        long count
) {
}
