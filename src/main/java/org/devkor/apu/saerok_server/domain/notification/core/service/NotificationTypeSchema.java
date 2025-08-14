package org.devkor.apu.saerok_server.domain.notification.core.service;

import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class NotificationTypeSchema {
    /** 현재 제공하는 알림 타입 목록 (필요시 enum만 추가하면 됨) */
    public Set<NotificationType> requiredTypes() {
        return EnumSet.of(
                NotificationType.LIKED_ON_COLLECTION,
                NotificationType.COMMENTED_ON_COLLECTION,
                NotificationType.SUGGESTED_BIRD_ID_ON_COLLECTION
        );
    }
}
