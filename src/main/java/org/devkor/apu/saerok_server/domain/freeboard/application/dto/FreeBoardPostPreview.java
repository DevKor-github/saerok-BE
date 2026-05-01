package org.devkor.apu.saerok_server.domain.freeboard.application.dto;

import java.time.LocalDateTime;

public record FreeBoardPostPreview(
        Long postId,
        Long userId,
        String nickname,
        String profileImageUrl,
        String thumbnailProfileImageUrl,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
