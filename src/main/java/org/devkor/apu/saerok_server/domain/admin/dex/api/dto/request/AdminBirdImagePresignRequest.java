package org.devkor.apu.saerok_server.domain.admin.dex.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AdminBirdImagePresignRequest(
        @Schema(description = "업로드할 도감 이미지 Content-Type", example = "image/jpeg")
        @NotBlank(message = "contentType을 입력해 주세요.")
        String contentType
) {
}
