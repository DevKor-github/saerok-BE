package org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "조류 도감 업데이트 동기화 응답 DTO")
@Data
public class BirdChangesResponse {

    @Schema(description = "요청 기준 시각", example = "2025-04-28T00:00:00+09:00")
    private OffsetDateTime since;

    @Schema(description = "새로 추가된 조류 목록")
    private List<BirdFullSyncResponse.BirdProfileItem> created;

    @Schema(description = "내용이 변경된 조류 목록")
    private List<BirdFullSyncResponse.BirdProfileItem> updated;

    @Schema(description = "삭제된 조류 ID 목록", example = "[99, 102, 150]")
    private List<Long> deletedIds;

    public boolean hasNoChanges() {
        return created.isEmpty() && updated.isEmpty() && deletedIds.isEmpty();
    }
}