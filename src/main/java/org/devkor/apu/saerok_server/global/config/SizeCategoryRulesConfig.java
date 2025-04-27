package org.devkor.apu.saerok_server.global.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "size-category-rules")
public class SizeCategoryRulesConfig {

    private int version;
    private List<Boundary> boundaries;
    private Map<String, String> labels;

    @Data
    @AllArgsConstructor
    public static class Boundary {
        private String category;
        private Double maxCm;
    }
}
