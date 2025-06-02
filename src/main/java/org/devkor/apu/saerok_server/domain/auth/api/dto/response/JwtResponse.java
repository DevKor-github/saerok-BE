package org.devkor.apu.saerok_server.domain.auth.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT 토큰 및 회원가입 상태 응답")
public record JwtResponse(

        @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "회원가입 상태", example = "COMPLETED")
        String signupStatus
) {
}
