package org.devkor.apu.saerok_server.domain.admin.report.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 삭제 사유 요청 DTO")
public record AdminDeleteReasonRequest(
        @Schema(description = "삭제 사유", example = "욕설/혐오 발언 포함으로 삭제")
        String reason
) { }
