package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateCollectionCommentResponse(

        @Schema(description = "댓글 ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        Long commentId,

        @Schema(description = "수정된 내용", example = "정보 감사합니다! 다음에도 기대할게요.", requiredMode = Schema.RequiredMode.REQUIRED)
        String content

) {
}
