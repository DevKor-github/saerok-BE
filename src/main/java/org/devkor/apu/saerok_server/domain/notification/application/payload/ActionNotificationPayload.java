package org.devkor.apu.saerok_server.domain.notification.application.payload;

import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

import java.util.Map;

public record ActionNotificationPayload(
        NotificationType type,
        Long recipientId,
        Long actorId,
        String actorName, // 표시용 이름
        Long relatedId,
        Map<String, Object> extras
) implements NotificationPayload { }
