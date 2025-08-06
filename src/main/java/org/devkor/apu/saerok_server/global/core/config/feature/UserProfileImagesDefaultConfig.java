package org.devkor.apu.saerok_server.global.core.config.feature;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "user-profile-images-default")
public class UserProfileImagesDefaultConfig {

    private String contentType;
    private List<String> keys;
}
