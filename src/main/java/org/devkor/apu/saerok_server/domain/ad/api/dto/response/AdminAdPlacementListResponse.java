package org.devkor.apu.saerok_server.domain.ad.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "광고 배치 목록 응답")
public record AdminAdPlacementListResponse(

        @Schema(description = "배치 목록")
        List<Item> items
) {

    @Schema(name = "AdminAdPlacementListResponse.Item", description = "광고 배치 항목")
    public record Item(
            Long id,
            Long adId,
            String adName,
            Long slotId,
            String slotName,
            LocalDate startDate,
            LocalDate endDate,
            Short weight,
            Boolean enabled,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
    }
}
