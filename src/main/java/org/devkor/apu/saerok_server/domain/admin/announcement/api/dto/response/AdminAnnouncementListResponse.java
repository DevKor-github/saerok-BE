package org.devkor.apu.saerok_server.domain.admin.announcement.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.entity.AnnouncementStatus;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "공지사항 목록 응답")
public record AdminAnnouncementListResponse(
        List<Item> announcements
) {

    @Schema(name = "AdminAnnouncementListResponse.Item")
    public record Item(
            @Schema(description = "공지사항 ID", example = "1")
            Long id,

            @Schema(description = "제목", example = "정기 점검 안내")
            String title,

            @Schema(description = "상태", example = "SCHEDULED")
            AnnouncementStatus status,

            @Schema(description = "게시 예정 시각(KST)", example = "2024-11-01T09:00:00+09:00")
            OffsetDateTime scheduledAt,

            @Schema(description = "게시 시각", example = "2024-11-01T09:00:00+09:00")
            OffsetDateTime publishedAt,

            @Schema(description = "작성 관리자 닉네임", example = "운영자A")
            String adminName
    ) {}
}
