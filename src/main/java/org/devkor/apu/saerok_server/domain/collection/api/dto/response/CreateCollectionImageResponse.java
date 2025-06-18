package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateCollectionImageResponse(

        @Schema(
                description = "저장된 컬렉션 이미지의 고유 ID입니다.",
                example = "103"
        )
        Long imageId,

        @Schema(
                description = "업로드된 이미지에 접근할 수 있는 CloudFront 기반의 정적 이미지 URL입니다.",
                example = "https://d123456abcdef.cloudfront.net/collection-images/42/uuid.jpg"
        )
        String url

) {}