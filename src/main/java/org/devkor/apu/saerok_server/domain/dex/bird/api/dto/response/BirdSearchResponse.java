package org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "도감 조회 및 검색 응답 DTO")
@Data
public class BirdSearchResponse {

    @Schema(description = "도감 검색 전체 목록")
    private List<BirdSearchItem> birds;

    @Data
    @Schema(description = "도감 검색 항목")
    public static class BirdSearchItem {
        @Schema(description = "조류 ID", example = "1")
        public Long id;

        @Schema(description = "한글 이름", example = "까치")
        public String koreanName;

        @Schema(description = "학명", example = "Pica pica")
        public String scientificName;

        @Schema(description = "썸네일 이미지 URL", example = "https://example.com/images/bird-thumb.jpg")
        public String thumbImageUrl;
    }
}
