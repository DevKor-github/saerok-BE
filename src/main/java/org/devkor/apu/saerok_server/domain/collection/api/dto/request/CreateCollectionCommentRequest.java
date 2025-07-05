package org.devkor.apu.saerok_server.domain.collection.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateCollectionCommentRequest(
        @Schema(description = "댓글 내용", example = "멋진 사진이네요!", requiredMode = Schema.RequiredMode.REQUIRED)
        String content
) {
}
