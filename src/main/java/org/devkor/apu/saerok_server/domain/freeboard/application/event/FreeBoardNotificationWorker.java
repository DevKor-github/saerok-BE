package org.devkor.apu.saerok_server.domain.freeboard.application.event;

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
public class FreeBoardNotificationWorker {

    private final NotifyActionDsl notifyAction;

    @Async("pushNotificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FreeBoardNotificationEvent.CommentCreated event) {
        try {
            Actor actor = Actor.of(event.actorId(), event.actorNickname());

            if (event.parentCommentId() != null) {
                // 대댓글: 원댓글 작성자에게 REPLY 알림
                if (!event.parentCommentOwnerId().equals(event.actorId())) {
                    notifyAction
                            .by(actor)
                            .on(Target.freeBoardComment(event.parentCommentId()))
                            .did(ActionKind.REPLY)
                            .comment(event.commentContent())
                            .to(event.parentCommentOwnerId());
                }
                // 게시글 소유자에게 COMMENT 알림 (원댓글 작성자와 다른 경우에만)
                if (!event.postOwnerId().equals(event.actorId())
                        && !event.postOwnerId().equals(event.parentCommentOwnerId())) {
                    notifyAction
                            .by(actor)
                            .on(Target.freeBoardPost(event.postId()))
                            .did(ActionKind.COMMENT)
                            .comment(event.commentContent())
                            .to(event.postOwnerId());
                }
            } else {
                // 원댓글: 게시글 소유자에게 COMMENT 알림
                if (!event.postOwnerId().equals(event.actorId())) {
                    notifyAction
                            .by(actor)
                            .on(Target.freeBoardPost(event.postId()))
                            .did(ActionKind.COMMENT)
                            .comment(event.commentContent())
                            .to(event.postOwnerId());
                }
            }
        } catch (Exception e) {
            log.error("Failed to send freeboard comment notification: postId={}, actorId={}",
                    event.postId(), event.actorId(), e);
        }
    }
}
