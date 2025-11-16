package org.devkor.apu.saerok_server.domain.admin.ad.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "광고 생성 요청")
public record AdminCreateAdRequest(

        @Schema(description = "관리자용 광고 이름", example = "홈 상단 배너", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String name,

        @Schema(description = "관리자 메모", example = "봄 시즌 캠페인")
        String memo,

        @Schema(description = "광고 이미지 object key", example = "ads/banner_123.png", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String objectKey,

        @Schema(description = "이미지 MIME 타입", example = "image/png", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String contentType,

        @Schema(description = "클릭 시 이동할 URL", example = "https://partner.com/promo", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String targetUrl
) {
}
