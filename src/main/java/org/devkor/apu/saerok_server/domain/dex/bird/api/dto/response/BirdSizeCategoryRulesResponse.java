package org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "조류 크기 카테고리 규칙 응답 DTO")
@Data
public class BirdSizeCategoryRulesResponse {

    @Schema(description = "규칙 버전", example = "1")
    private int version;

    @Schema(
            description = "카테고리별 경계 리스트",
            example = "[" +
                    "{\"category\": \"small\", \"maxCm\": 20}," +
                    "{\"category\": \"medium\", \"maxCm\": 60}," +
                    "{\"category\": \"large\", \"maxCm\": 100}," +
                    "{\"category\": \"xlarge\", \"maxCm\": null}" +
                    "]"
    )
    private List<Boundary> boundaries;

    @Schema(
            description = "카테고리별 표시 라벨 매핑",
            example = "{\"small\": \"참새 크기\", \"medium\": \"비둘기 크기\", \"large\": \"기러기 크기\", \"xlarge\": \"고니 크기\"}"
    )
    private Map<String, String> labels;

    @Schema(description = "카테고리별 경계 정보")
    @Data
    public static class Boundary {

        @Schema(description = "카테고리 코드", example = "small")
        private String category;

        @Schema(description = "최대 몸길이(cm). null이면 무한대", example = "20")
        private Double maxCm;
    }
}
