package org.devkor.apu.saerok_server.global.core.config.feature;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * image-variants.yml 을 바인딩하는 설정 클래스.
 *
 * 예시(yml):
 * image-variants:
 *   kinds:
 *     USER_COLLECTION_IMAGE:
 *       variants:
 *         - name: THUMBNAIL
 *           strategy: REPLACE_EXTENSION
 *           target-extension: webp
 *           prefix: thumbnails/
 *     DEX_BIRD_IMAGE:
 *       variants: []
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "image-variants")
public class ImageVariantsConfig {

    private Map<String, Kind> kinds = Collections.emptyMap();

    @Getter
    @Setter
    public static class Kind {
        private List<Variant> variants = Collections.emptyList();
    }

    @Getter
    @Setter
    public static class Variant {
        /**
         * 예: THUMBNAIL
         */
        private String name;

        /**
         * 예: REPLACE_EXTENSION (original.ext -> prefix + original + .targetExtension)
         */
        private String strategy;

        /**
         * 예: webp
         */
        private String targetExtension;

        /**
         * 예: thumbnails/
         */
        private String prefix;
    }
}
