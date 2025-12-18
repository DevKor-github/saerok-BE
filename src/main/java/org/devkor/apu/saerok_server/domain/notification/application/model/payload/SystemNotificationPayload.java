package org.devkor.apu.saerok_server.domain.notification.application.model.payload;

import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

import java.util.Map;

/**
 * 시스템 차원에서 발생하는 알림 payload.
 *
 * <p>예) 공지사항, 점검 안내, 신규 기능 안내 등</p>
 *
 * <p>
 * 시스템 알림은 행동 알림과 달리 NotificationSubject/NotificationAction을 사용하지 않습니다.
 * </p>
 */
public record SystemNotificationPayload(
        Long recipientId,
        NotificationType type,
        Long relatedId,
        String title,
        String body,
        Map<String, Object> extras
) implements NotificationPayload {

    public SystemNotificationPayload {
        extras = (extras == null) ? Map.of() : Map.copyOf(extras);
    }
}
