package org.devkor.apu.saerok_server.domain.notification.core.service;

import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

public final class NotificationTypeResolver {

    private NotificationTypeResolver() {}

    public static NotificationType from(NotificationSubject subject, NotificationAction action) {
        if (subject == null || action == null) {
            // subject/action 기반이 아닌 요청은 허용하지 않는다(그룹 토글 폐기).
            throw new IllegalArgumentException("subject/action must be non-null to resolve NotificationType");
        }
        // 현재 지원 케이스: subject == COLLECTION
        return switch (subject) {
            case COLLECTION -> switch (action) {
                case LIKE -> NotificationType.LIKED_ON_COLLECTION;
                case COMMENT -> NotificationType.COMMENTED_ON_COLLECTION;
                case SUGGEST_BIRD_ID -> NotificationType.SUGGESTED_BIRD_ID_ON_COLLECTION;
            };
        };
    }
}
