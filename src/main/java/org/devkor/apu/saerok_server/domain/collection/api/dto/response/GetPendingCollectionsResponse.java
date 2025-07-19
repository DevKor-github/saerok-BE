package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "bird_id 미확정 PUBLIC 컬렉션 목록 응답")
public record GetPendingCollectionsResponse(
        @Schema(description = "미확정 컬렉션 목록")
        List<Item> items
) {

    @Schema(name = "GetPendingCollectionsResponse.Item", description = "미확정 bird_id 컬렉션 개별 항목")
    public record Item(
            @Schema(description = "컬렉션 ID", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
            Long collectionId,

            @Schema(description = "대표 이미지 URL", example = "https://cdn.saerok.dev/images/abc.jpg")
            String imageUrl,

            @Schema(description = "한 줄 평", example = "처음 보는 새 발견")
            String note,

            @Schema(description = "작성자 닉네임", example = "새덕후99", requiredMode = Schema.RequiredMode.REQUIRED)
            String nickname,

            @Schema(description = "동정 의견 요청 시각", example = "2025-07-20T00:23:58.164815", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDateTime birdIdSuggestionRequestedAt
    ) {}
}
