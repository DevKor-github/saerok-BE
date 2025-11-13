package org.devkor.apu.saerok_server.domain.ad.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "슬롯 광고 응답")
public record GetAdSlotResponse(

        @Schema(description = "응답 타입", example = "AD", requiredMode = Schema.RequiredMode.REQUIRED)
        String type,

        @Schema(description = "클라이언트가 광고를 유지해야 하는 TTL(초)", example = "120", requiredMode = Schema.RequiredMode.REQUIRED)
        int ttlSeconds,

        @Schema(description = "광고 정보 (type이 AD일 때만 존재)")
        AdPayload ad
) {

    @Schema(name = "GetAdSlotResponse.AdPayload", description = "광고 정보")
    public record AdPayload(
            @Schema(description = "광고 ID", example = "123")
            Long id,

            @Schema(description = "광고 이미지 URL", example = "https://cdn.saerok.dev/ads/banner_123.png")
            String imageUrl,

            @Schema(description = "광고 클릭 시 이동할 URL", example = "https://partner.com/promo")
            String targetUrl
    ) {
    }
}
