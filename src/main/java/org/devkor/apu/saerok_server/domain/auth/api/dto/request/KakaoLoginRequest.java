package org.devkor.apu.saerok_server.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KakaoLoginRequest {

    @Schema(description = "인가 코드")
    private String authorizationCode;

    @Schema(description = "액세스 토큰")
    private String accessToken;

}
