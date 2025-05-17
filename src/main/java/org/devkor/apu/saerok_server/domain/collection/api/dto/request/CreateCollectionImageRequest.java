package org.devkor.apu.saerok_server.domain.collection.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateCollectionImageRequest(

        @Schema(
                description = "S3에 업로드된 이미지의 object key입니다. Presigned URL 발급 시 응답받은 값을 그대로 전달해야 합니다.",
                example = "collection-images/42/15fd9a32-bb4e-4b7c-bd8b-4fd1e2b3d8a4",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String objectKey,

        @Schema(
                description = "업로드한 이미지의 Content-Type입니다. 예: image/jpeg, image/png 등.",
                example = "image/png",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String contentType

) {
}