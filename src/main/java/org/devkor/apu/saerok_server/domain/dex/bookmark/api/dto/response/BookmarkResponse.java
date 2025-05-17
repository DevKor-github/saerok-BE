package org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Schema(description = "북마크 정보 응답")
public class BookmarkResponse {

    @Schema(description = "북마크 ID")
    private Long id;
    
    @Schema(description = "조류 ID")
    private Long birdId;
}
