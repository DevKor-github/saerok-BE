package org.devkor.apu.saerok_server.domain.notification.application.facade;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.application.helper.CollectionImageUrlService;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
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
    private final CollectionRepository collectionRepository;
    private final CollectionImageUrlService collectionImageUrlService;

    public StepOn by(Actor actor) {
        return new StepOn(publisher, collectionRepository, collectionImageUrlService, actor);
    }

    public static class StepOn {
        private final NotificationPublisher publisher;
        private final CollectionRepository collectionRepository;
        private final CollectionImageUrlService collectionImageUrlService;
        private final Actor actor;

        StepOn(NotificationPublisher p,
               CollectionRepository cr,
               CollectionImageUrlService ciu,
               Actor a) {
            this.publisher = p;
            this.collectionRepository = cr;
            this.collectionImageUrlService = ciu;
            this.actor = a;
        }

        public StepDid on(Target target) {
            return new StepDid(publisher, collectionRepository, collectionImageUrlService, actor, target);
        }
    }

    public static class StepDid {
        private final NotificationPublisher publisher;
        private final CollectionRepository collectionRepository;
        private final CollectionImageUrlService collectionImageUrlService;
        private final Actor actor;
        private final Target target;

        StepDid(NotificationPublisher p,
                CollectionRepository cr,
                CollectionImageUrlService ciu,
                Actor a,
                Target t) {
            this.publisher = p;
            this.collectionRepository = cr;
            this.collectionImageUrlService = ciu;
            this.actor = a;
            this.target = t;
        }

        public StepTo did(ActionKind kind) {
            return new StepTo(publisher, collectionRepository, collectionImageUrlService, actor, target, kind, new HashMap<>());
        }
    }

    public static class StepTo {
        private final NotificationPublisher publisher;
        private final CollectionRepository collectionRepository;
        private final CollectionImageUrlService collectionImageUrlService;
        private final Actor actor;
        private final Target target;
        private final ActionKind action;
        private final Map<String, Object> extras;

        StepTo(NotificationPublisher p,
               CollectionRepository cr,
               CollectionImageUrlService ciu,
               Actor a,
               Target t,
               ActionKind k,
               Map<String, Object> ex) {
            this.publisher = p;
            this.collectionRepository = cr;
            this.collectionImageUrlService = ciu;
            this.actor = a;
            this.target = t;
            this.action = k;
            this.extras = ex;
        }

        // 선택 파라미터들
        public StepTo comment(String content) {
            extras.put("comment", content);
            return this;
        }

        public StepTo suggestedName(String name) {
            extras.put("suggestedName", name);
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

            // TargetType별 extras 구성
            switch (target.type()) {
                case COLLECTION -> {
                    extras.put("collectionId", target.id());

                    String imageUrl = collectionRepository.findById(target.id())
                            .flatMap(collectionImageUrlService::getPrimaryImageUrlFor)
                            .orElse(null);
                    extras.put("collectionImageUrl", imageUrl);
                }
            }

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
