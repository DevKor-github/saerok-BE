package org.devkor.apu.saerok_server.domain.notification.application.assembly.render;

import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;

public interface NotificationRenderer {
    RenderedMessage render(NotificationPayload payload);

    /**
     * <h2>inAppBody</h2><p>인앱 알림 목록에 저장될 본문 (body-only)</p>
     * <h2>pushTitle / pushBody</h2><p>푸시 알림용 타이틀/본문</p>
     */
    record RenderedMessage(String inAppBody, String pushTitle, String pushBody) { }
}
