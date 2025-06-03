package org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Schema(description = "북마크된 조류 상세 정보 응답")
public class BookmarkedBirdDetailResponse {

    @Schema(description = "북마크 ID")
    private Long id;
    
    @Schema(description = "조류 ID")
    private Long birdId;
    
    @Schema(description = "조류 이름 (국문)")
    private String koreanName;
    
    @Schema(description = "조류 이름 (학명)")
    private String scientificName;
}
