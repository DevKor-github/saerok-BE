package org.devkor.apu.saerok_server.domain.ad.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "슬롯 생성 요청")
public record AdminCreateSlotRequest(

        @Schema(description = "슬롯 이름", example = "HOME_TOP", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String name,

        @Schema(description = "폴백 확률(0.0~1.0)", example = "0.2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Double fallbackRatio,

        @Schema(description = "TTL(초)", example = "120", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Integer ttlSeconds
) {
}
