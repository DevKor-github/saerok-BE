package org.devkor.apu.saerok_server.domain.dex.bird.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "조류 상세 응답 DTO")
public class BirdDetailResponse {
    @Schema(description = "조류 ID", example = "1")
    public Long id;

    @Schema(description = "한글 이름", example = "까치")
    public String koreanName;

    @Schema(description = "학명", example = "Pica pica")
    public String scientificName;

    @Schema(description = "분류학적 정보")
    public BirdTaxonomy taxonomy;

    @Schema(description = "설명", example = "전국 어디서나 흔하게 관찰되는 텃새입니다.")
    public String description;

    @Schema(description = "이미지 URL 목록", example = "[\"~~~.jpg\", \"~~~.png\"]")
    public List<String> imageUrls;

    @Schema(description = "크기 카테고리", example = "참새 크기")
    public String sizeCategory;

    @Schema(description = "서식지 목록", example = "[\"FARMLAND\", \"MARINE\"]")
    public List<String> habitats;

    @Schema(description = "국내 관찰 가능 계절 목록")
    public List<SeasonWithRarity> seasonsWithRarity;

    @Schema(description = "분류학적 정보")
    public static class BirdTaxonomy {
        public String phylumEng;
        public String phylumKor;
        public String classEng;
        public String classKor;
        public String orderEng;
        public String orderKor;
        public String familyEng;
        public String familyKor;
        public String genusEng;
        public String genusKor;
        public String speciesEng;
        public String speciesKor;
    }

    @Schema(description = "계절별 희귀도 정보")
    public static class SeasonWithRarity {
        @Schema(description = "계절", example = "SPRING")
        public String season;

        @Schema(description = "희귀도", example = "COMMON")
        public String rarity;

        @Schema(description = "우선순위", example = "10")
        public Integer priority;
    }
}
