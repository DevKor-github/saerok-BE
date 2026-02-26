package org.devkor.apu.saerok_server.global.core.config.feature;

import lombok.Getter;
import lombok.Setter;
import org.devkor.apu.saerok_server.domain.collection.core.entity.CommentStatus;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "comment-replacements")
@Getter
@Setter
public class CommentReplacementConfig {
    private Map<CommentStatus, String> contents = new EnumMap<>(CommentStatus.class);

    public String getReplacement(CommentStatus status) {
        return contents.getOrDefault(status, null);
    }
}
