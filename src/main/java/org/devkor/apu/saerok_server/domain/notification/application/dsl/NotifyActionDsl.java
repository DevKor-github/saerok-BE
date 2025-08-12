package org.devkor.apu.saerok_server.domain.notification.application.dsl;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.NotificationPublisher;
import org.devkor.apu.saerok_server.domain.notification.application.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotifyActionDsl {

    private final NotificationPublisher publisher;

    public StepOn by(Actor actor) { return new StepOn(publisher, actor); }

    public static class StepOn {
        private final NotificationPublisher publisher;
        private final Actor actor;
        StepOn(NotificationPublisher p, Actor a){ this.publisher = p; this.actor = a; }
        public StepDid on(Target target){ return new StepDid(publisher, actor, target); }
    }

    public static class StepDid {
        private final NotificationPublisher publisher;
        private final Actor actor;
        private final Target target;
        StepDid(NotificationPublisher p, Actor a, Target t){ this.publisher = p; this.actor = a; this.target = t; }
        public StepTo did(ActionKind kind){ return new StepTo(publisher, actor, target, kind, new HashMap<>()); }
    }

    public static class StepTo {
        private final NotificationPublisher publisher;
        private final Actor actor;
        private final Target target;
        private final ActionKind action;
        private final Map<String,Object> extras;

        StepTo(NotificationPublisher p, Actor a, Target t, ActionKind k, Map<String,Object> ex) {
            this.publisher = p; this.actor = a; this.target = t; this.action = k; this.extras = ex;
        }

        // 선택 파라미터들
        public StepTo comment(String content){ extras.put("comment", content); return this; }
        public StepTo suggestedName(String name){ extras.put("suggestedName", name); return this; }

        public void to(Long recipientId){
            NotificationType type = switch (action) {
                case LIKE -> NotificationType.LIKE;
                case COMMENT -> NotificationType.COMMENT;
                case BIRD_ID_SUGGESTION -> NotificationType.BIRD_ID_SUGGESTION;
            };

            publisher.push(
                    new ActionNotificationPayload(
                            type, recipientId, actor.id(), actor.name(), target.id(), extras
                    ),
                    target
            );
        }
    }
}
