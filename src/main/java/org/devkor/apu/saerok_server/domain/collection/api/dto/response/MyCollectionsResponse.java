package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내 컬렉션 목록 조회 응답")
public record MyCollectionsResponse(
        List<Item> items
) {

    @Schema(name = "MyCollectionsResponse.Item")
    public record Item(
        @Schema(description = "컬렉션 ID", example = "1")
        Long collectionId,

        @Schema(description = "이미지 URL", example = "https://example.com/images/collection1.jpg")
        String imageUrl,

        @Schema(description = "새의 한국 이름", example = "까치")
        String koreanName,

        @Schema(description = "좋아요 수", example = "15")
        Integer likeCount
    ) { }
}