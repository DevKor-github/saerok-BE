package org.devkor.apu.saerok_server.domain.notification.application.model.payload;

import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;

import java.util.Map;

public record ActionNotificationPayload(
        Long recipientId,
        Long actorId,
        String actorName,
        Long relatedId,
        NotificationSubject subject,
        NotificationAction action,
        Map<String, Object> extras
) implements NotificationPayload { }
