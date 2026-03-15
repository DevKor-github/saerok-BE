package org.devkor.apu.saerok_server.domain.freeboard.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateFreeBoardPostCommentRequest(
        @Schema(description = "댓글 내용", example = "좋은 글이네요!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String content,

        @Schema(description = "부모 댓글 ID (대댓글 작성 시)", example = "123", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long parentId
) {
}
