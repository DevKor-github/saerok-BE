package org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "북마크 목록 조회 응답")
public record BookmarkResponse(
        List<Item> items
) {

    @Schema(name = "BookmarkResponse.Item")
    public record Item(
        @Schema(description = "북마크 ID", example = "1")
        Long id,
        
        @Schema(description = "조류 ID", example = "10")
        Long birdId
    ) { }
}
