package org.devkor.apu.saerok_server.domain.announcement.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "게시된 공지사항 목록 응답")
public record AnnouncementListResponse(
        List<Item> announcements
) {

    @Schema(name = "AnnouncementListResponse.Item")
    public record Item(
            @Schema(description = "공지사항 ID", example = "1")
            Long id,

            @Schema(description = "공지사항 제목", example = "새 기능 안내")
            String title,

            @Schema(description = "게시 시각", example = "2024-10-12T09:00:00")
            LocalDateTime publishedAt
    ) {}
}
