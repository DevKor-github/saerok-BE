package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record GetCollectionCommentsResponse(
    List<Item> items
) {

    public record Item(
            @Schema(description = "댓글 ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
            Long commentId,
            @Schema(description = "작성자 ID", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
            Long userId,
            @Schema(description = "댓글 내용", example = "멋진 관찰 기록이네요!", requiredMode = Schema.RequiredMode.REQUIRED)
            String content,
            @Schema(description = "작성 시각", example = "2025-07-05 03:10:00", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDateTime createdAt,
            @Schema(description = "수정 시각", example = "2025-07-05 04:21:00", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDateTime updatedAt
    ) {

    }
}
