package org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "북마크 상태 응답")
public class BookmarkStatusResponse {

    @Schema(description = "조류 ID")
    private Long birdId;
    
    @Schema(description = "북마크 여부", example = "true")  // true: 북마크 됨
    private boolean bookmarked;
}
