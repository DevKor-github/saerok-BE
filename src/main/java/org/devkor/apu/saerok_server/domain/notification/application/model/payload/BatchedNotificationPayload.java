package org.devkor.apu.saerok_server.domain.notification.application.model.payload;

import org.devkor.apu.saerok_server.domain.notification.application.model.batch.BatchActor;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationTypeResolver;

import java.util.List;
import java.util.Map;

/**
 * 배치 처리된 알림 payload.
 * 여러 액터의 행동을 하나로 묶어서 전달한다.
 */
public record BatchedNotificationPayload(
        Long recipientId,
        NotificationSubject subject,
        NotificationAction action,
        Long relatedId,
        List<BatchActor> actors,
        int actorCount,
        Map<String, Object> extras
) implements NotificationPayload {

    public BatchedNotificationPayload {
        extras = (extras == null) ? Map.of() : Map.copyOf(extras);
    }

    @Override
    public NotificationType type() {
        return NotificationTypeResolver.from(subject, action);
    }

    public BatchActor getFirstActor() {
        if (actors.isEmpty()) {
            throw new IllegalStateException("Batch has no actors");
        }
        return actors.getFirst();
    }

    public boolean isSingleActor() {
        return actorCount == 1;
    }
}
