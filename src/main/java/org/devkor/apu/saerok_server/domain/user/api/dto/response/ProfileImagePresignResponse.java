package org.devkor.apu.saerok_server.domain.user.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileImagePresignResponse(

        @Schema(
                description = "클라이언트가 이미지 파일을 직접 PUT 요청으로 업로드할 수 있는 S3 Presigned URL입니다.",
                example = "https://example-bucket.s3.amazonaws.com/user-profile-images/42/uuid?X-Amz-Algorithm=AWS4-HMAC-SHA256&..."
        )
        String presignedUrl,

        @Schema(
                description = "업로드한 이미지의 고유 식별 키입니다. 이후 프로필 업데이트 시 이 값을 profileImageObjectKey로 사용해야 합니다.",
                example = "user-profile-images/99999/15fd9a32-bb4e-4b7c-bd8b-4fd1e2b3d8a4"
        )
        String objectKey

) {
}
