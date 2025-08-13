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

        return switch (a.type()) {
            case LIKE -> new RenderedMessage(
                    a.actorName() + "님이 좋아요를 눌렀어요!",     // inAppBody
                    a.actorName() + "님이 좋아요를 눌렀어요!",     // pushTitle (짧고 즉각적인 정보)
                    "나의 새록을 좋아해요"                          // pushBody  (보조 설명)
            );
            case COMMENT -> {
                String comment = String.valueOf(a.extras().getOrDefault("comment", ""));
                yield new RenderedMessage(
                        a.actorName() + "님이 댓글을 남겼어요: " + comment, // inAppBody
                        a.actorName() + "님이 댓글을 남겼어요!",             // pushTitle
                        comment                                              // pushBody
                );
            }
            case BIRD_ID_SUGGESTION -> {
                String suggested = String.valueOf(a.extras().getOrDefault("suggestedName", ""));
                yield new RenderedMessage(
                        a.actorName() + "님이 동정 의견을 제안했어요: " + suggested, // inAppBody
                        a.actorName() + "님이 동정 의견을 제안했어요!",              // pushTitle
                        suggested                                                    // pushBody
                );
            }
            case SYSTEM -> new RenderedMessage(
                    "시스템 알림",   // inAppBody
                    "시스템 알림",   // pushTitle
                    ""               // pushBody
            );
        };
    }
}
