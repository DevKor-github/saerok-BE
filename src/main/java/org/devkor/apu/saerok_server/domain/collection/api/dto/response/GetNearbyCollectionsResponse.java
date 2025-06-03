package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "주위의 컬렉션 조회 응답 DTO")
public class GetNearbyCollectionsResponse {

    @Schema(description = "주위의 컬렉션 목록")
    private List<Item> items;

    @Data
    public static class Item {
        @Schema(description = "컬렉션 ID", example = "1")
        private Long collectionId;

        @Schema(description = "이미지 URL", example = "https://cdn.example.com/collection-images/1.jpg")
        private String imageUrl;

        @Schema(description = "새 한국어 이름", example = "까치")
        private String koreanName;

        @Schema(description = "한 줄 평", example = "광화문에서 까치가 날아다녔어요")
        private String note;
    }
}