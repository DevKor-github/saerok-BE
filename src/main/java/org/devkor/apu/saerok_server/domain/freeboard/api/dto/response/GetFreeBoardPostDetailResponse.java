package org.devkor.apu.saerok_server.domain.freeboard.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record GetFreeBoardPostDetailResponse(
        @Schema(description = "게시글 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long postId,
        @Schema(description = "작성자 ID", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        Long userId,
        @Schema(description = "작성자 닉네임", example = "새록마스터", requiredMode = Schema.RequiredMode.REQUIRED)
        String nickname,
        @Schema(description = "작성자 프로필 이미지 URL", requiredMode = Schema.RequiredMode.REQUIRED)
        String profileImageUrl,
        @Schema(description = "작성자 썸네일 프로필 이미지 URL (320px 너비)", requiredMode = Schema.RequiredMode.REQUIRED)
        String thumbnailProfileImageUrl,
        @Schema(description = "게시글 내용", example = "오늘 한강공원에서 백로를 발견했어요!", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,
        @Schema(description = "댓글 수", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        long commentCount,
        @Schema(description = "내 게시글인지 여부 (비로그인 시 false)", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean isMine,
        @Schema(description = "작성 시각", example = "2025-07-05T03:10:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime createdAt,
        @Schema(description = "수정 시각", example = "2025-07-05T04:21:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime updatedAt
) {
}
