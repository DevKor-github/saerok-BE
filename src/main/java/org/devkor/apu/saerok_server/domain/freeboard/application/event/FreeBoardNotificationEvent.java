package org.devkor.apu.saerok_server.domain.freeboard.application.event;

public sealed interface FreeBoardNotificationEvent {

    record CommentCreated(
            Long actorId, String actorNickname,
            Long postId, Long postOwnerId,
            Long parentCommentId, Long parentCommentOwnerId,
            String commentContent
    ) implements FreeBoardNotificationEvent {}
}
