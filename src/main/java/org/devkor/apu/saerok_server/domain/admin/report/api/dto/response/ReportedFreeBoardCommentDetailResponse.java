package org.devkor.apu.saerok_server.domain.admin.report.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostDetailResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostCommentsResponse;

import java.time.LocalDateTime;

@Schema(description = "신고된 자유게시판 댓글 상세(댓글 + 부모 게시글 + 댓글 목록)")
public record ReportedFreeBoardCommentDetailResponse(
        Long reportId,
        ReportedComment comment,
        GetFreeBoardPostDetailResponse post,
        GetFreeBoardPostCommentsResponse comments
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
