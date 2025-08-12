package org.devkor.apu.saerok_server.domain.notification.application.render;

import org.devkor.apu.saerok_server.domain.notification.application.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class ActionNotificationRenderer implements NotificationRenderer {

    @Override
    public RenderedNotification render(NotificationPayload p) {
        if (!(p instanceof ActionNotificationPayload a)) {
            throw new IllegalArgumentException("Unsupported payload: " + p.getClass());
        }

        return switch (a.type()) {
            case LIKE -> new RenderedNotification(
                    a.actorName() + "님이 좋아요를 눌렀어요!",
                    "나의 새록을 좋아해요"
            );
            case COMMENT -> new RenderedNotification(
                    a.actorName() + "님이 댓글을 남겼어요!",
                    String.valueOf(a.extras().getOrDefault("comment", ""))
            );
            case BIRD_ID_SUGGESTION -> new RenderedNotification(
                    a.actorName() + "님이 동정 의견을 제안했어요!",
                    String.valueOf(a.extras().getOrDefault("suggestedName", ""))
            );
            case SYSTEM -> new RenderedNotification("시스템 알림", "");
        };
    }
}
