package org.devkor.apu.saerok_server.domain.auth.infra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoErrorResponse {

    @JsonProperty("error")
    private String error; // /oauth/token 에러 필드

    @JsonProperty("error_description")
    private String errorDescription; // /oauth/token 에러 필드

    @JsonProperty("error_code")
    private String errorCode; // /oauth/token 에러 필드

    @JsonProperty("msg")
    private String msg; // /v2/user/me 에러 필드
}
