package org.devkor.apu.saerok_server.domain.notification.core.entity;

/**
 * 클라이언트에서 최종 식별자로 사용하는 알림 타입.
 *
 * <p>
 * Action 알림은 Subject/Action을 조합해 Type으로 해석됩니다.
 * System 알림은 Type을 직접 지정합니다.
 * </p>
 */
public enum NotificationType {

    // ---- Action Notification Types ----
    LIKED_ON_COLLECTION,
    COMMENTED_ON_COLLECTION,
    SUGGESTED_BIRD_ID_ON_COLLECTION,

    // ---- System Notification Types ----
    SYSTEM_PUBLISHED_ANNOUNCEMENT
}
