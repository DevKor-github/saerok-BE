package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.global.shared.util.dto.ExtractedImageMetadata;

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
        String url,
        
        @Schema(
                description = "이미지에서 추출된 메타데이터 정보 (날짜, GPS 좌표 등)"
        )
        ExtractedImageMetadata extractedMetadata

) {}