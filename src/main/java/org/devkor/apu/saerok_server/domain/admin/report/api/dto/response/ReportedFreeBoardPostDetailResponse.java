package org.devkor.apu.saerok_server.domain.admin.report.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostDetailResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostCommentsResponse;

@Schema(description = "신고된 자유게시판 게시글 상세(게시글 + 댓글 목록)")
public record ReportedFreeBoardPostDetailResponse(
        Long reportId,
        GetFreeBoardPostDetailResponse post,
        GetFreeBoardPostCommentsResponse comments
) {}
