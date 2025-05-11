package org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "북마크 토글 응답")
public class BookmarkToggleResponse {

    @Schema(description = "조류 ID")
    private Long birdId;
    
    @Schema(description = "수행된 작업", example = "added")  // "added": 추가됨, "removed": 제거됨
    private String action;
}
