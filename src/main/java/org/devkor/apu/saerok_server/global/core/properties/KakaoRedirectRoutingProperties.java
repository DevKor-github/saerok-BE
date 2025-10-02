package org.devkor.apu.saerok_server.global.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 채널별로 허용된 Kakao redirect_uri를 보관하는 프로퍼티.
 */
@Data
@Component
@ConfigurationProperties(prefix = "oauth.kakao.redirect-routing")
public class KakaoRedirectRoutingProperties {

    /** channel이 없을 때 사용할 기본 redirect_uri */
    private String defaultRedirectUri;

    /** channel → redirect_uri */
    private Map<String, String> channels = new HashMap<>();
}
