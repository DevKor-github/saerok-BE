package org.devkor.apu.saerok_server.domain.admin.announcement.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "공지사항 이미지 Presigned URL 발급 요청")
public record AdminAnnouncementImagePresignRequest(

        @Schema(description = "업로드할 이미지 MIME 타입", example = "image/png", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String contentType
) {
}
