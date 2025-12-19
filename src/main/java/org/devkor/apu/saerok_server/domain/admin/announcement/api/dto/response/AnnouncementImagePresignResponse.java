package org.devkor.apu.saerok_server.domain.admin.announcement.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공지사항 이미지 업로드용 Presigned URL 응답")
public record AnnouncementImagePresignResponse(
        @Schema(description = "이미지 업로드용 Presigned URL", example = "https://s3...signed-url")
        String presignedUrl,

        @Schema(description = "업로드할 object key", example = "announcements/uuid.png")
        String objectKey
) {
}
