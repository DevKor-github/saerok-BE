package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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

        @Schema(description = "썸네일 이미지 URL (320px 너비)", example = "https://cdn.example.com/thumbnails/abc.webp")
        String thumbnailImageUrl,

        @Schema(description = "새의 한국 이름", example = "까치")
        String koreanName,

        @Schema(description = "좋아요 수", example = "15")
        Long likeCount,

        @Schema(description = "댓글 수", example = "7")
        Long commentCount,

        @Schema(description = "컬렉션 등록 일시", example = "2025-01-15T14:30:00+09:00")
        OffsetDateTime createdAt,

        @Schema(description = "새 발견 일시", example = "2025-01-15")
        LocalDate discoveredDate
    ) { }
}