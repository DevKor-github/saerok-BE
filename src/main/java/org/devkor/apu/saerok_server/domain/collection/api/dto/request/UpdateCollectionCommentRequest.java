package org.devkor.apu.saerok_server.domain.collection.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateCollectionCommentRequest(
        @Schema(description = "수정할 댓글 내용", example = "우와", requiredMode = Schema.RequiredMode.REQUIRED)
        String content
) {
}
