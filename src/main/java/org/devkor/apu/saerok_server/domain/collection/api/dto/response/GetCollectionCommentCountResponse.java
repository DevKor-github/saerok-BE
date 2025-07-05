package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GetCollectionCommentCountResponse(
        @Schema(description = "댓글 수", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
        long count
) {
}
