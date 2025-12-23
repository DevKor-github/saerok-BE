package org.devkor.apu.saerok_server.global.core.config.feature;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 알림 배치 처리 설정.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "notification-batch")
public class NotificationBatchConfig {

    private boolean enabled = true; // 배치 처리 활성화 여부.
    private int initialWindowSeconds = 30;
    private int maxWindowSeconds = 60;
    private int ttlSeconds = 90;

    @PostConstruct
    void validateConfig() {
        if (initialWindowSeconds <= 0) {
            throw new IllegalStateException("notification-batch.initial-window-seconds는 양수여야합니다");
        }
        if (maxWindowSeconds <= 0) {
            throw new IllegalStateException("notification-batch.max-window-seconds는 양수여야합니다");
        }
        if (maxWindowSeconds < initialWindowSeconds) {
            throw new IllegalStateException(
                    String.format("notification-batch.max-window-seconds (%d) >= initial-window-seconds (%d) 이어야 합니다",
                            maxWindowSeconds, initialWindowSeconds)
            );
        }
        if (ttlSeconds <= maxWindowSeconds) {
            throw new IllegalStateException(
                    String.format("notification-batch.ttl-seconds (%d) > max-window-seconds (%d) 이어야 합니다",
                            ttlSeconds, maxWindowSeconds)
            );
        }
    }
}
