package org.devkor.apu.saerok_server.domain.notification.application.facade;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.SystemNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
     * @param title       푸시 타이틀
     * @param body        푸시 바디
     * @param relatedId   관련 엔티티 id (없으면 null)
     * @param extras      인앱 payload에 저장될 추가 데이터 (nullable)
     */
    public void notifyUser(
            Long recipientId,
            NotificationType type,
            String title,
            String body,
            Long relatedId,
            Map<String, Object> extras
    ) {
        Map<String, Object> merged = new HashMap<>();
        if (extras != null) merged.putAll(extras);

        // 기본적으로 title/body를 payload에도 담아두면 FE 렌더링이나 히스토리 조회가 편함
        if (title != null) merged.putIfAbsent("title", title);
        if (body != null) merged.putIfAbsent("body", body);

        publisher.push(new SystemNotificationPayload(
                recipientId,
                type,
                relatedId,
                title,
                body,
                merged
        ));
    }
}
