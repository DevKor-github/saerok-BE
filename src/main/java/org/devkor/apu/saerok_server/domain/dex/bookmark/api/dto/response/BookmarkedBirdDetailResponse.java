package org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Schema(description = "북마크된 조류 상세 정보 응답")
public class BookmarkedBirdDetailResponse {

    @Schema(description = "북마크 ID")
    private Long bookmarkId;
    
    @Schema(description = "조류 ID")
    private Long birdId;
    
    @Schema(description = "조류 이름 (국문)")
    private String koreanName;
    
    @Schema(description = "조류 이름 (학명)")
    private String scientificName;
    
    @Schema(description = "조류 설명")
    private String description;
    
    @Schema(description = "체장 (cm)")
    private Double bodyLengthCm;
    
    @Schema(description = "조류 이미지 URL 목록")
    private List<String> imageUrls;
    
    @Schema(description = "북마크 생성 시간")
    private OffsetDateTime bookmarkedAt;
}
