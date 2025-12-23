package org.devkor.apu.saerok_server.domain.admin.announcement.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "공지사항 이미지 정보")
public record AdminAnnouncementImageRequest(
        @Schema(description = "S3 object key", example = "announcements/2024/notice-1.png", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String objectKey,

        @Schema(description = "이미지 MIME 타입", example = "image/png", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String contentType
) {
}
