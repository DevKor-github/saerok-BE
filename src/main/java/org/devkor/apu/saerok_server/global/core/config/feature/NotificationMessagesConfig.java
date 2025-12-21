package org.devkor.apu.saerok_server.global.core.config.feature;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "notification-messages")
public class NotificationMessagesConfig {

    /** enum 키로 바인딩 (YAML 키는 NotificationType.name()) */
    private Map<NotificationType, Template> types = new EnumMap<>(NotificationType.class);

    public Template forType(NotificationType type) {
        Template t = types.get(type);
        if (t == null) {
            throw new IllegalArgumentException("해당 NotificationType을 위한 메시지 템플릿이 없습니다: " + type.name());
        }
        return t;
    }

    /** 기동 시 누락된 템플릿 검증 */
    @PostConstruct
    void validateAllTypesPresent() {
        var required = EnumSet.allOf(NotificationType.class);
        required.removeAll(types.keySet());
        if (!required.isEmpty()) {
            throw new IllegalStateException("notification-messages.types에 누락된 NotificationType이 있습니다: " + required);
        }
    }

    @Getter @Setter
    public static class Template {
        private String pushTitle;
        private String pushBody;
        private String inAppBody;

        // 배치 알림용 템플릿
        private String batchPushTitle;
        private String batchPushBody;
    }
}
