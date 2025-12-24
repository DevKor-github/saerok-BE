package org.devkor.apu.saerok_server.domain.admin.announcement.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.entity.AnnouncementStatus;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "공지사항 단일 응답")
public record AdminAnnouncementDetailResponse(
        @Schema(description = "공지사항 ID", example = "1")
        Long id,

        @Schema(description = "제목", example = "정기 점검 안내")
        String title,

        @Schema(description = "내용(HTML)", example = "<p>점검 안내</p>")
        String content,

        @Schema(description = "상태", example = "SCHEDULED")
        AnnouncementStatus status,

        @Schema(description = "예약 게시 시각(KST)", example = "2025-01-01T12:00:00+09:00")
        OffsetDateTime scheduledAt,

        @Schema(description = "게시 시각(KST)", example = "2025-01-01T12:00:00+09:00")
        OffsetDateTime publishedAt,

        @Schema(description = "알림 발송 여부", example = "true")
        Boolean sendNotification,

        @Schema(description = "푸시 알림 제목", example = "새 공지사항이 등록되었어요")
        String pushTitle,

        @Schema(description = "푸시 알림 본문", example = "공지사항을 확인해 주세요.")
        String pushBody,

        @Schema(description = "인앱 알림 본문", example = "공지사항이 게시되었습니다.")
        String inAppBody,

        @Schema(description = "작성자(관리자) 닉네임", example = "admin")
        String adminName,

        @Schema(description = "본문 이미지 정보")
        List<Image> images
) {
    public record Image(
            @Schema(description = "이미지 object key", example = "announcements/uuid.png")
            String objectKey,

            @Schema(description = "이미지 MIME 타입", example = "image/png")
            String contentType,

            @Schema(description = "이미지 접근 URL (CDN 도메인 기반)", example = "https://cdn.../announcements/uuid.png")
            String imageUrl
    ) {}
}
