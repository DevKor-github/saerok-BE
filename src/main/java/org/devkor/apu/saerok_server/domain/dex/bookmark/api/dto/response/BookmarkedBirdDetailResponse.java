package org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "북마크된 조류 상세 정보 응답")
public record BookmarkedBirdDetailResponse(
    @Schema(description = "북마크 ID")
    Long id,
    
    @Schema(description = "조류 ID")
    Long birdId,
    
    @Schema(description = "조류 이름 (국문)")
    String koreanName,
    
    @Schema(description = "조류 이름 (학명)")
    String scientificName
) {}
