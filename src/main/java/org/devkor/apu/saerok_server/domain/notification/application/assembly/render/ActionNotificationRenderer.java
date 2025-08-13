package org.devkor.apu.saerok_server.domain.notification.application.assembly.render;

import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.springframework.stereotype.Component;

@Component
public class ActionNotificationRenderer implements NotificationRenderer {

    @Override
    public RenderedMessage render(NotificationPayload p) {
        if (!(p instanceof ActionNotificationPayload a)) {
            throw new IllegalArgumentException("Unsupported payload: " + p.getClass());
        }

        return switch (a.action()) {
            case LIKE -> new RenderedMessage(
                    a.actorName() + "님이 좋아요를 눌렀어요",   // inApp body
                    "새 좋아요 알림",                         // push title
                    "누가 내 새록을 좋아했는지 확인해보세요"     // push body
            );
            case COMMENT -> new RenderedMessage(
                    a.actorName() + "님이 댓글을 남겼어요",
                    "새 댓글 알림",
                    String.valueOf(a.extras().getOrDefault("comment", ""))
            );
            case SUGGEST_BIRD_ID -> new RenderedMessage(
                    a.actorName() + "님이 동정 의견을 제안했어요",
                    "새 동정 의견 알림",
                    String.valueOf(a.extras().getOrDefault("suggestedName", ""))
            );
        };
    }
}
