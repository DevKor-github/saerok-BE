package org.devkor.apu.saerok_server.domain.ad.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "광고 수정 요청")
public record AdminUpdateAdRequest(

        @Schema(description = "관리자용 광고 이름", example = "홈 상단 배너 v2")
        String name,

        @Schema(description = "관리자 메모", example = "카피 수정")
        String memo,

        @Schema(description = "광고 이미지 object key", example = "ads/banner_123_v2.png")
        String objectKey,

        @Schema(description = "이미지 MIME 타입", example = "image/webp")
        String contentType,

        @Schema(description = "클릭 시 이동할 URL", example = "https://partner.com/promo2")
        String targetUrl
) {
}
