package org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "북마크 토글 응답")
public class BookmarkToggleResponse {

    @Schema(description = "조류 ID")
    private Long birdId;
    
    @Schema(description = "토글 후 북마크 상태", example = "true")  // true: 북마크 됨
    private boolean bookmarked;
}
