package org.devkor.apu.saerok_server.domain.admin.announcement.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "공지사항 생성 요청")
public record AdminCreateAnnouncementRequest(

        @Schema(description = "공지사항 제목", example = "정기 점검 안내", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 255)
        String title,

        @Schema(description = "공지사항 본문 (HTML)", example = "<p>공지 내용</p>", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String content,

        @Schema(description = "KST 기준 게시 예정 시각 (즉시 게시 시 null)", example = "2024-11-01T09:00:00")
        LocalDateTime scheduledAt,

        @Schema(description = "즉시 게시 여부", example = "false")
        Boolean publishNow,

        @Schema(description = "공지사항 게시 시 알림 발송 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Boolean sendNotification,

        @Schema(description = "푸시 알림 제목 (알림 발송 시 필수)", example = "새 공지사항이 게시되었습니다.")
        String pushTitle,

        @Schema(description = "푸시 알림 본문 (알림 발송 시 필수)", example = "공지사항을 확인해 주세요.")
        String pushBody,

        @Schema(description = "인앱 알림 본문 (알림 발송 시 필수)", example = "새 공지사항이 올라왔어요.")
        String inAppBody,

        @Schema(description = "본문에 포함될 이미지 정보 목록")
        @Valid
        List<AdminAnnouncementImageRequest> images
) {
}
