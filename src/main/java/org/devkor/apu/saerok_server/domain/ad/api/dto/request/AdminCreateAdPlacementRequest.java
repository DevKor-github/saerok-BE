package org.devkor.apu.saerok_server.domain.ad.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "광고 배치 생성 요청")
public record AdminCreateAdPlacementRequest(

        @Schema(description = "광고 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Long adId,

        @Schema(description = "슬롯 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Long slotId,

        @Schema(description = "노출 시작 날짜(YYYY-MM-DD)", example = "2025-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        LocalDate startDate,

        @Schema(description = "노출 종료 날짜(YYYY-MM-DD)", example = "2025-01-31", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        LocalDate endDate,

        @Schema(description = "노출 가중치(1~5)", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Short weight,

        @Schema(description = "활성 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Boolean enabled
) {
}
