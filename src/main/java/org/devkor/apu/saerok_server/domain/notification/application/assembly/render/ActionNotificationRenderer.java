package org.devkor.apu.saerok_server.domain.notification.application.assembly.render;

import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationTypeResolver;
import org.springframework.stereotype.Component;

@Component
public class ActionNotificationRenderer implements NotificationRenderer {

    @Override
    public RenderedMessage render(NotificationPayload p) {
        if (!(p instanceof ActionNotificationPayload a)) {
            throw new IllegalArgumentException("Unsupported payload: " + p.getClass());
        }

        NotificationType type = NotificationTypeResolver.from(a.subject(), a.action());

        return switch (type) {
            case LIKED_ON_COLLECTION -> new RenderedMessage(
                    a.actorName() + "님이 나의 새록을 좋아해요.",   // inApp body
                    a.actorName(),                         // push title
                    "나의 새록을 좋아해요."     // push body
            );
            case COMMENTED_ON_COLLECTION -> new RenderedMessage(
                    a.actorName() + "님이 나의 새록에 댓글을 남겼어요. \"" + a.extras().getOrDefault("comment", "") +"\"",
                    a.actorName(),
                    "나의 새록에 댓글을 남겼어요. \"" + a.extras().getOrDefault("comment", "") + "\""
            );
            case SUGGESTED_BIRD_ID_ON_COLLECTION -> new RenderedMessage(
                    "두근두근! 새로운 의견이 공유되었어요. 확인해볼까요?",
                    "동정 의견 공유",
                    "두근두근! 새로운 의견이 공유되었어요. 확인해볼까요?"
            );
        };
    }
}
