package org.devkor.apu.saerok_server.domain.ad.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "슬롯 수정 요청")
public record AdminUpdateSlotRequest(

        @Schema(description = "폴백 확률(0.0~1.0)", example = "0.5")
        Double fallbackRatio,

        @Schema(description = "TTL(초)", example = "60")
        Integer ttlSeconds
) {
}
