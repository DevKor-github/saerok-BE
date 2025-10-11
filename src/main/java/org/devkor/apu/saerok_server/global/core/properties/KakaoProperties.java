package org.devkor.apu.saerok_server.global.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kakao")
public class KakaoProperties {

    private String clientId;
    private String redirectUri;
    private String clientSecret;
    private String adminKey;

    /**
     * Kakao 앱의 고유 숫자 ID.
     * /v1/user/access_token_info 응답의 appId와 일치해야 한다.
     * application.yml 에서는 kakao.app-id 로 설정한다.
     */
    private Long appId;
}
