package org.devkor.apu.saerok_server.domain.admin.ad.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdImagePresignResponse(

        @Schema(
                description = "클라이언트가 광고 이미지를 직접 PUT 요청으로 업로드할 수 있는 S3 Presigned URL입니다.",
                example = "https://example-bucket.s3.amazonaws.c.../ad-images/uuid?X-Amz-Algorithm=AWS4-HMAC-SHA256&..."
        )
        String presignedUrl,

        @Schema(
                description = "업로드한 광고 이미지의 고유 S3 Object Key입니다. 이후 광고 생성/수정 시 이 값을 objectKey로 사용해야 합니다.",
                example = "ad-images/15fd9a32-bb4e-4b7c-bd8b-4fd1e2b3d8a4"
        )
        String objectKey

) {}
