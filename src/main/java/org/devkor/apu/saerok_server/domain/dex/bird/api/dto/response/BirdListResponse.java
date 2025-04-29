package org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "조류 목록 응답 DTO")
public class BirdListResponse {
    @Schema(description = "조류 ID", example = "1")
    public Long id;

    @Schema(description = "한글 이름", example = "까치")
    public String koreanName;

    @Schema(description = "학명", example = "Pica pica")
    public String scientificName;

    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/images/bird-thumb.jpg")
    public String thumbImageUrl;
}
