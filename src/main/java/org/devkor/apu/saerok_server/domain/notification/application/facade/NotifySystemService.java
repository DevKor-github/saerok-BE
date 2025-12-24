package org.devkor.apu.saerok_server.domain.notification.application.facade;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.SystemNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 시스템 알림 발송을 위한 간단한 파사드.
 *
 * <p>
 * 행동 알림은 NotifyActionDsl이 담당하고,
 * 시스템 알림은 (Subject/Action 없이) 이 서비스로 발송합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class NotifySystemService {

    private final NotificationPublisher publisher;

    /**
     * 특정 사용자에게 시스템 알림을 전송합니다.
     *
     * @param recipientId 알림 수신자
     * @param type        SYSTEM_* 타입 권장 (현재는 SYSTEM_NOTICE)
     * @param relatedId   관련 엔티티 id (없으면 null)
     * @param extras      인앱 payload에 저장될 추가 데이터 (nullable)
     */
    public void notifyUser(
            Long recipientId,
            NotificationType type,
            Long relatedId,
            Map<String, Object> extras
    ) {
        Map<String, Object> merged = new HashMap<>();
        if (extras != null) merged.putAll(extras);

        publisher.push(new SystemNotificationPayload(
                recipientId,
                type,
                relatedId,
                merged
        ));
    }

    /**
     * 여러 사용자에게 시스템 알림을 전송하되, 푸시는 디바이스 기준으로 중복 제거합니다.
     */
    public void notifyUsersDeduplicatedPush(
            List<Long> recipientIds,
            NotificationType type,
            Long relatedId,
            Map<String, Object> extras
    ) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            return;
        }

        Map<String, Object> merged = new HashMap<>();
        if (extras != null) merged.putAll(extras);

        List<SystemNotificationPayload> payloads = recipientIds.stream()
                .map(recipientId -> new SystemNotificationPayload(
                        recipientId,
                        type,
                        relatedId,
                        merged
                ))
                .toList();

        publisher.pushDeduplicatedByDevice(payloads);
    }
}
