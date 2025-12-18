package org.devkor.apu.saerok_server.domain.notification.application.gateway;

import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

public interface PushGateway {

    /**
     * 특정 사용자에게 푸시 메시지를 전송합니다.
     *
     * <p>디바이스/설정 필터링(알림 타입별 on/off)은 구현체에서 처리합니다.</p>
     */
    void sendToUser(Long userId, NotificationType type, PushMessageCommand cmd);

    /**
     * iOS 배지 업데이트용 silent push를 전송합니다.
     */
    void sendSilentBadgeUpdate(Long userId, int unreadCount);
}
