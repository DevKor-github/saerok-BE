package org.devkor.apu.saerok_server.domain.notification.application.model.payload;

import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationTypeResolver;

import java.util.Map;

/**
 * 다른 사용자(actor)의 특정 행동이 대상 사용자(recipient)에게 전달되는 행동 알림 payload.
 */
public record ActionNotificationPayload(
        Long recipientId,
        Long actorId,
        String actorName,
        NotificationSubject subject,
        NotificationAction action,
        Long relatedId,
        Map<String, Object> extras
) implements NotificationPayload {

    public ActionNotificationPayload {
        extras = (extras == null) ? Map.of() : Map.copyOf(extras);
    }

    @Override
    public NotificationType type() {
        return NotificationTypeResolver.from(subject, action);
    }
}
