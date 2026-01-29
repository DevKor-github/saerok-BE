package org.devkor.apu.saerok_server.domain.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 완료 요청 DTO")
public record SignupCompleteRequest(
        @Schema(description = "사용자 닉네임", example = "새록이")
        String nickname,

        @Schema(description = "회원가입 경로", example = "INSTAGRAM", allowableValues = {"INSTAGRAM", "OTHER_SNS", "FRIEND", "COMMUNITY", "ETC"})
        String signupSource
) {
}
