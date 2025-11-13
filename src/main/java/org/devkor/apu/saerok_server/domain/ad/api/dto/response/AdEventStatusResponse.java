package org.devkor.apu.saerok_server.domain.ad.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "광고 이벤트 처리 결과")
public record AdEventStatusResponse(

        @Schema(description = "처리 상태", example = "ok")
        String status
) {
}
