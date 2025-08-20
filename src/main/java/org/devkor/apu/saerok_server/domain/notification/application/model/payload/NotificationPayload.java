package org.devkor.apu.saerok_server.domain.notification.application.model.payload;

import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;

import java.util.Map;

public sealed interface NotificationPayload
        permits ActionNotificationPayload /*, SystemNotificationPayload 등 확장 여지 */ {

    NotificationSubject subject();
    NotificationAction action();
    Long recipientId();           // 알림 받을 사람
    Map<String, Object> extras(); // 메타데이터 (ex. comment, collectionId, collectionImageUrl ...)
}
