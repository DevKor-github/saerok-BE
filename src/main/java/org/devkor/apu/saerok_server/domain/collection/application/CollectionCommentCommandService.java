package org.devkor.apu.saerok_server.domain.collection.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.request.CreateCollectionCommentRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.request.UpdateCollectionCommentRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.CreateCollectionCommentResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.UpdateCollectionCommentResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.ActionKind;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Actor;
import org.devkor.apu.saerok_server.domain.notification.application.facade.NotifyActionDsl;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectionCommentCommandService {

    private final CollectionCommentRepository commentRepository;
    private final CollectionRepository       collectionRepository;
    private final UserRepository             userRepository;
    private final NotifyActionDsl notifyAction;

    /* 댓글 작성 */
    public CreateCollectionCommentResponse createComment(Long userId,
                                                         Long collectionId,
                                                         CreateCollectionCommentRequest req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        UserBirdCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));

        UserBirdCollectionComment comment;
        UserBirdCollectionComment parentComment = null;

        if (req.commentId() != null) {
            parentComment = commentRepository.findById(req.commentId())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글 id예요"));

            // 같은 컬렉션에 속하는지 확인
            if (!parentComment.getCollection().getId().equals(collectionId)) {
                throw new NotFoundException("해당 컬렉션에 속한 댓글이 아니에요");
            }

            if (!parentComment.isActive()) {
                throw new ForbiddenException("삭제된 댓글에는 대댓글을 작성할 수 없어요");
            }

            // depth 1 제한
            if (parentComment.isReply()) {
                throw new ForbiddenException("대댓글에는 답글을 작성할 수 없어요");
            }

            comment = UserBirdCollectionComment.of(user, collection, req.content(), parentComment);
        } else {
            comment = UserBirdCollectionComment.of(user, collection, req.content());
        }

        commentRepository.save(comment);

        // 알림 전송
        if (parentComment != null) {
            // 1. 원댓글 작성자에게 REPLY 알림 (자신의 댓글이 아닌 경우)
            if (!parentComment.getUser().getId().equals(userId)) {
                notifyAction
                        .by(Actor.of(userId, user.getNickname()))
                        .on(Target.comment(parentComment.getId()))
                        .did(ActionKind.REPLY)
                        .comment(req.content())
                        .to(parentComment.getUser().getId());
            }

            // 2. 컬렉션 작성자에게 COMMENT 알림 (자신의 컬렉션이 아닌 경우)
            if (!collection.getUser().getId().equals(userId)) {
                notifyAction
                        .by(Actor.of(userId, user.getNickname()))
                        .on(Target.collection(collectionId))
                        .did(ActionKind.COMMENT)
                        .comment(req.content())
                        .to(collection.getUser().getId());
            }
        } else {
            // 원댓글: 컬렉션 소유자에게 알림 (자신의 컬렉션이 아닌 경우)
            if (!collection.getUser().getId().equals(userId)) {
                notifyAction
                        .by(Actor.of(userId, user.getNickname()))
                        .on(Target.collection(collectionId))
                        .did(ActionKind.COMMENT)
                        .comment(req.content())
                        .to(collection.getUser().getId());
            }
        }
        
        return new CreateCollectionCommentResponse(comment.getId());
    }

    /* 댓글 수정 */
    public UpdateCollectionCommentResponse updateComment(Long userId,
                                                         Long collectionId,
                                                         Long commentId,
                                                         UpdateCollectionCommentRequest req) {

        UserBirdCollectionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글 id예요"));

        if (!comment.getCollection().getId().equals(collectionId)) {
            throw new NotFoundException("해당 컬렉션에 속한 댓글이 아니에요");
        }
        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 댓글에 대한 수정 권한이 없어요");
        }

        comment.setContent(req.content());

        return new UpdateCollectionCommentResponse(
                comment.getId(),
                comment.getContent()
        );
    }

    /* 댓글 삭제 */
    public void deleteComment(Long userId, Long collectionId, Long commentId) {

        UserBirdCollectionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글 id예요"));

        if (!comment.getCollection().getId().equals(collectionId)) {
            throw new NotFoundException("해당 컬렉션에 속한 댓글이 아니에요");
        }
        
        // 댓글 작성자이거나 컬렉션 소유자인 경우에만 삭제 가능
        boolean isCommentOwner = comment.getUser().getId().equals(userId);
        boolean isCollectionOwner = comment.getCollection().getUser().getId().equals(userId);
        
        if (!isCommentOwner && !isCollectionOwner) {
            throw new ForbiddenException("해당 댓글에 대한 삭제 권한이 없어요");
        }

        commentRepository.remove(comment);
    }
}