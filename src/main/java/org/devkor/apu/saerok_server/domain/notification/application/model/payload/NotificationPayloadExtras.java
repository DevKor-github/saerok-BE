package org.devkor.apu.saerok_server.domain.notification.application.model.payload;

import java.util.Map;
import java.util.stream.Collectors;

final class NotificationPayloadExtras {

    private NotificationPayloadExtras() {
    }

    static Map<String, Object> sanitize(Map<String, Object> extras) {
        if (extras == null || extras.isEmpty()) {
            return Map.of();
        }

        return extras.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
