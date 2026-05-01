package org.devkor.apu.saerok_server.domain.collection.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.facade.NotifyActionDsl;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.ActionKind;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Actor;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionNotificationWorker {

    private final NotifyActionDsl notifyAction;

    @Async("pushNotificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CollectionNotificationEvent.CommentCreated event) {
        try {
            Actor actor = Actor.of(event.actorId(), event.actorNickname());

            if (event.parentCommentId() != null) {
                // 대댓글: 원댓글 작성자에게 REPLY 알림
                if (!event.parentCommentOwnerId().equals(event.actorId())) {
                    notifyAction
                            .by(actor)
                            .on(Target.comment(event.parentCommentId()))
                            .did(ActionKind.REPLY)
                            .comment(event.commentContent())
                            .to(event.parentCommentOwnerId());
                }
                // 컬렉션 소유자에게 COMMENT 알림 (원댓글 작성자와 다른 경우에만)
                if (!event.collectionOwnerId().equals(event.actorId())
                        && !event.collectionOwnerId().equals(event.parentCommentOwnerId())) {
                    notifyAction
                            .by(actor)
                            .on(Target.collection(event.collectionId()))
                            .did(ActionKind.COMMENT)
                            .comment(event.commentContent())
                            .to(event.collectionOwnerId());
                }
            } else {
                // 원댓글: 컬렉션 소유자에게 COMMENT 알림
                if (!event.collectionOwnerId().equals(event.actorId())) {
                    notifyAction
                            .by(actor)
                            .on(Target.collection(event.collectionId()))
                            .did(ActionKind.COMMENT)
                            .comment(event.commentContent())
                            .to(event.collectionOwnerId());
                }
            }
        } catch (Exception e) {
            log.error("Failed to send collection comment notification: collectionId={}, actorId={}",
                    event.collectionId(), event.actorId(), e);
        }
    }

    @Async("pushNotificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CollectionNotificationEvent.CollectionLiked event) {
        try {
            notifyAction
                    .by(Actor.of(event.actorId(), event.actorNickname()))
                    .on(Target.collection(event.collectionId()))
                    .did(ActionKind.LIKE)
                    .to(event.collectionOwnerId());
        } catch (Exception e) {
            log.error("Failed to send collection like notification: collectionId={}, actorId={}",
                    event.collectionId(), event.actorId(), e);
        }
    }

    @Async("pushNotificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CollectionNotificationEvent.BirdIdSuggested event) {
        try {
            notifyAction
                    .by(Actor.of(event.actorId(), event.actorNickname()))
                    .on(Target.collection(event.collectionId()))
                    .did(ActionKind.SUGGEST_BIRD_ID)
                    .suggestedName(event.suggestedBirdName())
                    .to(event.collectionOwnerId());
        } catch (Exception e) {
            log.error("Failed to send bird ID suggestion notification: collectionId={}, actorId={}",
                    event.collectionId(), event.actorId(), e);
        }
    }
}
