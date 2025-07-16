package org.devkor.apu.saerok_server.domain.collection.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReportCollectionResponse(
        @Schema(description = "신고 ID", example = "1")
        Long reportId
) {
}
