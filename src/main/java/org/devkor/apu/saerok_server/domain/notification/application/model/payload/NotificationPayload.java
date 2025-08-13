package org.devkor.apu.saerok_server.domain.notification.application.model.payload;

import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

import java.util.Map;

public sealed interface NotificationPayload
        permits ActionNotificationPayload /*, SystemNotificationPayload 등 확장 여지 */ {

    NotificationType type();
    Long recipientId();           // D: 알림 받을 사람
    Long relatedId();             // 관련 리소스 id (ex. collectionId)
    Map<String, Object> extras(); // 템플릿 변수 (코멘트 내용 등)
}
