package org.devkor.apu.saerok_server.domain.collection.application.event;

public sealed interface CollectionNotificationEvent {

    record CommentCreated(
            Long actorId, String actorNickname,
            Long collectionId, Long collectionOwnerId,
            Long parentCommentId, Long parentCommentOwnerId,
            String commentContent
    ) implements CollectionNotificationEvent {}

    record CollectionLiked(
            Long actorId, String actorNickname,
            Long collectionId, Long collectionOwnerId
    ) implements CollectionNotificationEvent {}

    record BirdIdSuggested(
            Long actorId, String actorNickname,
            Long collectionId, Long collectionOwnerId,
            String suggestedBirdName
    ) implements CollectionNotificationEvent {}
}
