package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "컬렉션에 제안된 bird_id 목록 응답")
public record GetBirdIdSuggestionsResponse(
        @Schema(description = "bird_id 제안 항목 목록")
        List<Item> items
) {

    @Schema(name = "GetBirdIdSuggestionsResponse.Item", description = "bird_id 제안 항목")
    public record Item(
            @Schema(description = "조류 ID", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
            Long birdId,

            @Schema(description = "조류의 한글 이름", example = "까치", requiredMode = Schema.RequiredMode.REQUIRED)
            String birdKoreanName,

            @Schema(description = "조류의 학명", example = "Pica pica", requiredMode = Schema.RequiredMode.REQUIRED)
            String birdScientificName,

            @Schema(description = "조류 대표 이미지 URL", example = "https://cdn.saerok.dev/birds/thumbs/101.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
            String birdImageUrl,

            @Schema(description = "해당 조류에 동의한 사용자 수", example = "8", requiredMode = Schema.RequiredMode.REQUIRED)
            Long agreeCount,

            @Schema(description = "현재 사용자가 동의했는지 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
            Boolean isAgreedByMe
    ) {}
}
