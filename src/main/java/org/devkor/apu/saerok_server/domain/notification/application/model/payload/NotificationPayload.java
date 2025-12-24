package org.devkor.apu.saerok_server.domain.notification.application.model.payload;

import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

import java.util.Map;

/**
 * <h2>알림 발송 파이프라인에서 사용하는 공통 Payload</h2>
 *
 * <ul>
 *   <li><b>ActionNotificationPayload</b>: 다른 사용자(actor)의 행동에 의해 발생하는 알림</li>
 *   <li><b>SystemNotificationPayload</b>: 공지/점검 등 시스템 차원에서 발생하는 알림</li>
 *   <li><b>BatchedNotificationPayload</b>: 여러 사용자의 행동을 모은 배치 알림</li>
 * </ul>
 *
 * <p>
 * 공통 파이프라인은 <code>type</code>, <code>relatedId</code>, <code>recipientId</code>, <code>extras</code>만 알면 되고,
 * Subject/Action 같은 행동 알림 전용 속성은 Action payload에만 존재합니다.
 * </p>
 */
public sealed interface NotificationPayload
        permits ActionNotificationPayload, SystemNotificationPayload, BatchedNotificationPayload {

    /**
     * 클라이언트에서 식별 가능한 최종 알림 타입.
     */
    NotificationType type();

    /**
     * 알림이 "무엇"과 관련된 것인지 표현하는 id.
     * <ul>
     *   <li>예) COLLECTION 알림이면 collectionId</li>
     * </ul>
     * 없을 경우 null 허용.
     */
    Long relatedId();

    /**
     * 알림을 받을 사용자 id.
     */
    Long recipientId();

    /**
     * 인앱 알림 payload(JSON)로 저장되는 추가 메타데이터.
     */
    Map<String, Object> extras();
}
