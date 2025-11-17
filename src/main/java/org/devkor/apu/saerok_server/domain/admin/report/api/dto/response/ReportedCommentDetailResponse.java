package org.devkor.apu.saerok_server.domain.admin.report.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionCommentsResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionDetailResponse;

import java.time.LocalDateTime;

@Schema(description = "신고된 댓글 상세(댓글 + 부모 새록 + 댓글 목록)")
public record ReportedCommentDetailResponse(
        Long reportId,
        ReportedComment comment,
        GetCollectionDetailResponse collection,
        GetCollectionCommentsResponse comments
) {
    public record ReportedComment(
            Long commentId,
            Long userId,
            String nickname,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
