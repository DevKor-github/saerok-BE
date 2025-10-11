package org.devkor.apu.saerok_server.domain.auth.infra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * GET https://kapi.kakao.com/v1/user/access_token_info
 * 로부터 받는 응답 모델.
 */
@Data
public class KakaoAccessTokenInfoResponse {

    /** 카카오 사용자 ID (sub) */
    @JsonProperty("id")
    private Long id;

    /** 이 토큰을 발급한 Kakao 앱의 숫자 ID */
    @JsonProperty("appId")
    private Long appId;

    /** 만료까지 남은 시간(ms). 카카오가 제공 */
    @JsonProperty("expiresInMillis")
    private Long expiresInMillis;
}
