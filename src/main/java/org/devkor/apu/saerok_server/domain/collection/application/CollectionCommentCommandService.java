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

    /* 댓글 작성 */
    public CreateCollectionCommentResponse createComment(Long userId,
                                                         Long collectionId,
                                                         CreateCollectionCommentRequest req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        UserBirdCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));

        UserBirdCollectionComment comment = UserBirdCollectionComment.of(user, collection, req.content());

        commentRepository.save(comment);
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
        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 댓글에 대한 삭제 권한이 없어요");
        }

        commentRepository.remove(comment);
    }
}