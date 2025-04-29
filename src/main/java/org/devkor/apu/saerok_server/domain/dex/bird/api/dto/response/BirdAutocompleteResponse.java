package org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "조류 자동완성 응답 DTO")
public class BirdAutocompleteResponse {
    @Schema(description = "추천 이름 리스트", example = "[\"까치\", \"까마귀\"]")
    public List<String> suggestions;
}
