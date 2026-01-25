package org.devkor.apu.saerok_server.domain.user.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.user.core.entity.SignupStatusType;

@Schema(description = "회원가입 완료 응답 DTO")
public record SignupCompleteResponse(
        @Schema(description = "사용자 ID", example = "42")
        Long userId,

        @Schema(description = "회원가입 상태", example = "COMPLETED")
        SignupStatusType signupStatus,

        @Schema(description = "성공 여부", example = "true")
        boolean success
) {
}
