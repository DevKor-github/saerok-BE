package org.devkor.apu.saerok_server.domain.ad.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminCreateSlotRequest {

    @Schema(description = "슬롯 이름 (예: HOME_TOP)", example = "HOME_TOP")
    @NotBlank
    private String name;

    @Schema(description = "관리자 메모", example = "홈 상단 배너 영역")
    private String memo;

    @Schema(description = "Fallback으로 기본 광고로 폴백할 확률 (0.0 ~ 1.0)", example = "0.2")
    @NotNull
    private Double fallbackRatio;

    @Schema(description = "클라이언트가 동일 광고를 유지해야 하는 TTL(초)", example = "120")
    @NotNull
    private Integer ttlSeconds;
}
