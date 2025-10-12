package org.devkor.apu.saerok_server.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KakaoLoginRequest {

    @Schema(description = "인가 코드")
    private String authorizationCode;

    @Schema(description = "액세스 토큰")
    private String accessToken;

    @Schema(description = "로그인 요청 주체 채널 (예: 'admin', 'user' 등)")
    private String channel;
}
