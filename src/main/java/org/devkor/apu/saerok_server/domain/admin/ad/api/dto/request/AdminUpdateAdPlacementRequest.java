package org.devkor.apu.saerok_server.domain.admin.ad.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "광고 배치 수정 요청")
public record AdminUpdateAdPlacementRequest(

        @Schema(description = "슬롯 ID", example = "2")
        Long slotId,

        @Schema(description = "노출 시작 날짜(YYYY-MM-DD)", example = "2025-02-01")
        LocalDate startDate,

        @Schema(description = "노출 종료 날짜(YYYY-MM-DD)", example = "2025-02-28")
        LocalDate endDate,

        @Schema(description = "노출 가중치(1~5)", example = "4")
        Short weight,

        @Schema(description = "활성 여부", example = "false")
        Boolean enabled
) {
}
