package org.devkor.apu.saerok_server.domain.freeboard.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateFreeBoardPostRequest(
        @Schema(description = "수정할 내용", example = "수정된 내용입니다", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String content
) {
}
