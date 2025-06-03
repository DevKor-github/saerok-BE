package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "내 컬렉션 목록 조회 응답")
public class MyCollectionsResponse {
    @Schema(description = "컬렉션 ID", example = "1")
    private Long collectionId;

    @Schema(description = "이미지 URL", example = "https://example.com/images/collection1.jpg")
    private String imageUrl;

    @Schema(description = "새 이름", example = "까치")
    private String birdName;
}