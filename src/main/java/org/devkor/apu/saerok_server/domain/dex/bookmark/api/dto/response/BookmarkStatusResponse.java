package org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "북마크 상태 응답")
public record BookmarkStatusResponse(
   @Schema(description = "조류 ID")
   Long birdId,
   
   @Schema(description = "북마크 여부", example = "true")
   boolean bookmarked
) {}
