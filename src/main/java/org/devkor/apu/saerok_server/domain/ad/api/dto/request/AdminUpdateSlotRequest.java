package org.devkor.apu.saerok_server.domain.ad.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminUpdateSlotRequest {

    @Schema(description = "관리자 메모", example = "홈 상단 배너 영역")
    private String memo;

    @Schema(description = "Fallback으로 기본 광고로 폴백할 확률 (0.0 ~ 1.0)", example = "0.3")
    private Double fallbackRatio;

    @Schema(description = "클라이언트가 동일 광고를 유지해야 하는 TTL(초)", example = "180")
    private Integer ttlSeconds;
}
