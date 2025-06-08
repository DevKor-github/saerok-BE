package org.devkor.apu.saerok_server.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KakaoLoginRequest {

    @Schema(description = "인가 코드")
    private String code;

    @Schema(description = "액세스 토큰 (카카오 인증 서버에서 받은 액세스 토큰)")
    private String accessToken;

    @Hidden
    private String error;

    @Hidden
    @Schema(name = "error_description")
    private String errorDescription;

    public void setError_description(String error_description) {
        this.errorDescription = error_description;
    }
    // 쿼리 파라미터 error_description을 String errorDescription 변수에 매핑하기 위함

}
