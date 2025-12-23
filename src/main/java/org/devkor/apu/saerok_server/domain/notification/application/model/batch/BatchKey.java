package org.devkor.apu.saerok_server.domain.notification.application.model.batch;

import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;

/**
 * 알림 배치를 식별하기 위한 키.
 */
public record BatchKey(
        Long recipientId,
        NotificationSubject subject,
        NotificationAction action,
        Long relatedId
) {
    /**
     * Redis 키 형식으로 변환.
     * 형식: notification:batch:{recipientId}:{subject}:{action}:{relatedId}
     */
    public String toRedisKey() {
        return String.format(
                "notification:batch:%d:%s:%s:%d",
                recipientId,
                subject.name(),
                action.name(),
                relatedId
        );
    }
}
