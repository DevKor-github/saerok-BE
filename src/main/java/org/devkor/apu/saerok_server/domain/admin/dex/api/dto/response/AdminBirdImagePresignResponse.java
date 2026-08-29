package org.devkor.apu.saerok_server.domain.admin.dex.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminBirdImagePresignResponse(
        @Schema(description = "클라이언트가 이미지를 PUT 업로드할 Presigned URL")
        String presignedUrl,

        @Schema(description = "업로드된 도감 이미지 S3 object key", example = "raw/uuid.jpg")
        String objectKey
) {
}
