package org.devkor.apu.saerok_server.domain.ad.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "광고 이벤트 요청 DTO")
public record AdEventRequest(

        @Schema(description = "광고 ID", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Long adId,

        @Schema(description = "슬롯 이름", example = "HOME_TOP", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String slotName,

        @Schema(description = "디바이스 ID (기기별로 고유하고 변하지 않는 식별자)", example = "ABC123-XYZ", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String deviceId
) {
}
