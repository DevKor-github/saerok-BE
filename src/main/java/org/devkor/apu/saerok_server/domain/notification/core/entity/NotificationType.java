package org.devkor.apu.saerok_server.domain.notification.core.entity;

/**
 * 클라이언트에 노출되는 단일 식별자(type).
 * 내부 (subject, action) 쌍을 아래 type으로 매핑해 사용한다.
 */
public enum NotificationType {
    LIKED_ON_COLLECTION,
    COMMENTED_ON_COLLECTION,
    SUGGESTED_BIRD_ID_ON_COLLECTION
    // SYSTEM 제거: 실데이터 없음(요청사항)
}
