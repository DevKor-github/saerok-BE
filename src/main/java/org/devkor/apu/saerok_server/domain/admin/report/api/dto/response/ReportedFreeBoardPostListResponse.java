package org.devkor.apu.saerok_server.domain.admin.report.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "신고된 자유게시판 게시글 목록 응답")
public record ReportedFreeBoardPostListResponse(
        List<Item> items
) {
    public record Item(
            Long reportId,
            LocalDateTime reportedAt,
            Long postId,
            String contentPreview,
            UserMini reporter,
            UserMini reportedUser
    ) {}

    public record UserMini(
            Long userId,
            String nickname
    ) {}
}
