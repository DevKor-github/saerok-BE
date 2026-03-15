package org.devkor.apu.saerok_server.domain.freeboard.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateFreeBoardPostRequest(
        @Schema(description = "게시글 내용", example = "오늘 한강공원에서 백로를 발견했어요!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String content
) {
}
