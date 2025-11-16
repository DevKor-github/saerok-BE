package org.devkor.apu.saerok_server.domain.admin.ad.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "슬롯 목록 응답")
public record AdminSlotListResponse(

        @Schema(description = "슬롯 목록")
        List<Item> items
) {

    @Schema(name = "AdminSlotListResponse.Item", description = "슬롯 항목")
    public record Item(
            Long id,
            String name,
            String memo,
            Double fallbackRatio,
            Integer ttlSeconds,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
    }
}
