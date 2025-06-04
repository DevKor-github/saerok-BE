package org.devkor.apu.saerok_server.domain.auth.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KakaoLoginRequest {

    private String code;
    private String error;

    @Schema(name = "error_description")
    private String errorDescription;

    public void setError_description(String error_description) {
        this.errorDescription = error_description;
    }
    // 쿼리 파라미터 error_description을 String errorDescription 변수에 매핑하기 위함

}
