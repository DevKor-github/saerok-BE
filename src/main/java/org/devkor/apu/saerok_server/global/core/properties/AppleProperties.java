package org.devkor.apu.saerok_server.global.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "apple")
public class AppleProperties {

    private String clientId;
    private String teamId;
    private String keyId;
    private String privateKey;
}
