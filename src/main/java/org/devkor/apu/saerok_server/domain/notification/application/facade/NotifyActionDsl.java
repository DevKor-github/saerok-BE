package org.devkor.apu.saerok_server.domain.notification.application.facade;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.ActionKind;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Actor;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.port.TargetMetadataPort;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotifyActionDsl {

    private final NotificationPublisher publisher;
    private final TargetMetadataPort targetMetadataPort;

    public StepOn by(Actor actor) {
        return new StepOn(publisher, targetMetadataPort, actor);
    }

    public static class StepOn {
        private final NotificationPublisher publisher;
        private final TargetMetadataPort targetMetadataPort;
        private final Actor actor;

        StepOn(NotificationPublisher p, TargetMetadataPort port, Actor a) {
            this.publisher = p;
            this.targetMetadataPort = port;
            this.actor = a;
        }

        /**
         * 대상 지정. 이 시점에 Target만으로 계산 가능한 메타(extras)를 확정한다.
         */
        public StepDid on(Target target) {
            Map<String, Object> base = targetMetadataPort.enrich(target, Map.of());
            return new StepDid(publisher, actor, target, new HashMap<>(base));
        }
    }

    public static class StepDid {
        private final NotificationPublisher publisher;
        private final Actor actor;
        private final Target target;
        private final Map<String, Object> baseExtras; // on()에서 확정된 기본 메타

        StepDid(NotificationPublisher p, Actor a, Target t, Map<String, Object> base) {
            this.publisher = p;
            this.actor = a;
            this.target = t;
            this.baseExtras = (base == null) ? new HashMap<>() : new HashMap<>(base);
        }

        /**
         * 수행한 액션 지정. on()에서 만든 메타를 이어받아 누적한다.
         */
        public StepTo did(ActionKind kind) {
            return new StepTo(publisher, actor, target, kind, new HashMap<>(baseExtras));
        }
    }

    public static class StepTo {
        private final NotificationPublisher publisher;
        private final Actor actor;
        private final Target target;
        private final ActionKind action;
        private final Map<String, Object> extras; // 누적되는 선택 파라미터

        StepTo(NotificationPublisher p,
               Actor a,
               Target t,
               ActionKind k,
               Map<String, Object> ex) {
            this.publisher = p;
            this.actor = a;
            this.target = t;
            this.action = k;
            this.extras = (ex == null) ? new HashMap<>() : new HashMap<>(ex);
        }

        public StepTo comment(String content) {
            if (content != null && !content.isEmpty()) {
                extras.put("comment", content);
            }
            return this;
        }

        public StepTo suggestedName(String name) {
            if (name != null && !name.isEmpty()) {
                extras.put("suggestedName", name);
            }
            return this;
        }

        public void to(Long recipientId) {
            var notificationSubject = switch (target.type()) {
                case COLLECTION -> NotificationSubject.COLLECTION;
            };

            var notificationAction = switch (action) {
                case LIKE -> NotificationAction.LIKE;
                case COMMENT -> NotificationAction.COMMENT;
                case SUGGEST_BIRD_ID -> NotificationAction.SUGGEST_BIRD_ID;
            };

            publisher.push(
                    new ActionNotificationPayload(
                            recipientId,
                            actor.id(),
                            actor.name(),
                            notificationSubject,
                            notificationAction,
                            extras
                    ),
                    target
            );
        }
    }
}
