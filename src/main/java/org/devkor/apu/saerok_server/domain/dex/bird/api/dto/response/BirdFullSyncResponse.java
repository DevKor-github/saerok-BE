package org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.devkor.apu.saerok_server.domain.dex.habitat.entity.HabitatType;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "조류 도감 전체 동기화 응답 DTO")
@Data
public class BirdFullSyncResponse {

    @Schema(description = "조류 도감 전체 목록")
    private List<BirdProfileItem> birds;

    @Data
    @Schema(description = "조류 도감 항목")
    public static class BirdProfileItem {

        @Schema(description = "조류 ID", example = "1")
        private Long id;

        @Schema(description = "조류 이름 관련 정보")
        private BirdName name;

        @Schema(description = "분류학적 정보")
        private BirdTaxonomy taxonomy;

        @Schema(description = "조류 설명 정보")
        private BirdDescription description;

        @Schema(description = "몸길이 (cm)", example = "14.5")
        private Double bodyLengthCm;

        @Schema(description = "NIBR URL", example = "http://nibr...")
        private String nibrUrl;

        @Schema(description = "서식지 목록")
        private List<HabitatType> habitats;

        @Schema(description = "계절별 희귀도 목록")
        private List<SeasonWithRarity> seasonsWithRarity;

        @Schema(description = "이미지 목록")
        private List<Image> images;

        @Schema(description = "최종 수정 시각")
        private OffsetDateTime updatedAt;

        @Data
        @Schema(description = "조류 이름 관련 정보")
        public static class BirdName {

            @Schema(description = "한국어 이름")
            private String koreanName;

            @Schema(description = "학명")
            private String scientificName;

            @Schema(description = "학명 명명자")
            private String scientificAuthor;

            @Schema(description = "학명 명명년도")
            private Integer scientificYear;
        }

        @Data
        @Schema(description = "분류학적 정보")
        public static class BirdTaxonomy {

            private String phylumEng;
            private String phylumKor;
            private String classEng;
            private String classKor;
            private String orderEng;
            private String orderKor;
            private String familyEng;
            private String familyKor;
            private String genusEng;
            private String genusKor;
            private String speciesEng;
            private String speciesKor;
        }

        @Data
        @Schema(description = "조류 설명 상세")
        public static class BirdDescription {

            @Schema(description = "설명 텍스트")
            private String description;

            @Schema(description = "설명 출처 URL 또는 정보")
            private String source;

            @Schema(description = "설명 AI 생성 여부")
            private Boolean isAiGenerated;
        }

        @Data
        @Schema(description = "계절별 희귀도 정보")
        public static class SeasonWithRarity {

            @Schema(description = "계절", example = "SPRING")
            private String season;

            @Schema(description = "희귀도", example = "COMMON")
            private String rarity;

            @Schema(description = "우선순위 값", example = "10")
            private Integer priority;
        }

        @Data
        @Schema(description = "이미지 정보")
        public static class Image {

            @Schema(description = "썸네일 여부")
            private Boolean isThumb;

            @Schema(description = "S3 저장 URL (이 URL을 사용해 표시해야 함)")
            private String s3Url;

            @Schema(description = "원본 이미지 URL (출처 보존용)")
            private String originalUrl;

            @Schema(description = "이미지 순서 인덱스", example = "0")
            private Integer orderIndex;
        }
    }
}
