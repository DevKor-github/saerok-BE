package org.devkor.apu.saerok_server.domain.ad.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "광고 목록 응답")
public record AdminAdListResponse(

        @Schema(description = "광고 목록")
        List<Item> items
) {

    @Schema(name = "AdminAdListResponse.Item", description = "광고 항목")
    public record Item(
            Long id,
            String name,
            String memo,
            String objectKey,
            String contentType,
            String targetUrl,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
    }
}
