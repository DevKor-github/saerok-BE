package org.devkor.apu.saerok_server.global.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "reserved-nicknames")
public class ReservedNicknamesConfig {

    private List<String> items;
}
