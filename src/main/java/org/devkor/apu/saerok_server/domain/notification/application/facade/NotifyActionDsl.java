package org.devkor.apu.saerok_server.domain.notification.application.facade;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.ActionKind;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Actor;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
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
            NotificationSubject notificationSubject = switch (target.type()) {
                case COLLECTION -> NotificationSubject.COLLECTION;
            };

            NotificationAction notificationAction = switch (action) {
                case LIKE -> NotificationAction.LIKE;
                case COMMENT -> NotificationAction.COMMENT;
                case SUGGEST_BIRD_ID -> NotificationAction.SUGGEST_BIRD_ID;
            };

            publisher.push(
                    new ActionNotificationPayload(
                            recipientId,
                            actor.id(),
                            actor.name(),
                            target.id(),
                            notificationSubject,
                            notificationAction,
                            extras
                    ),
                    target
            );
        }
    }
}
