package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내가 좋아요한 컬렉션 목록 조회 응답 DTO")
public record GetLikedCollectionsResponse(
        @Schema(description = "좋아요한 컬렉션 목록")
        List<Item> items
) {
    @Schema(name = "GetLikedCollectionsResponse.Item")
    public record Item(
            @Schema(description = "컬렉션 ID", example = "1")
            Long collectionId
    ) {}
}
