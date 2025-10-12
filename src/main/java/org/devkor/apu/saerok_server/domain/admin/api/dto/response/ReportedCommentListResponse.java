package org.devkor.apu.saerok_server.domain.admin.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "신고된 댓글 목록 응답")
public record ReportedCommentListResponse(
        List<Item> items
) {
    public record Item(
            Long reportId,
            LocalDateTime reportedAt,
            Long commentId,
            Long collectionId,
            String contentPreview,
            UserMini reporter,
            UserMini reportedUser
    ) {}

    public record UserMini(
            Long userId,
            String nickname
    ) {}
}
