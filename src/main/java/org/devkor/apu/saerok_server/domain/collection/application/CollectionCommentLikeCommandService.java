package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.LikeStatusResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionCommentLike;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentLikeRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectionCommentLikeCommandService {

    private final CollectionCommentLikeRepository collectionCommentLikeRepository;
    private final CollectionCommentRepository collectionCommentRepository;
    private final UserRepository userRepository;

    /**
     * 댓글 좋아요 토글 (추가/제거)
     */
    public LikeStatusResponse toggleLikeResponse(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        
        UserBirdCollectionComment comment = collectionCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));

        boolean exists = collectionCommentLikeRepository.existsByUserIdAndCommentId(userId, commentId);

        if (exists) {
            // 좋아요가 이미 존재하면 제거
            UserBirdCollectionCommentLike commentLike = collectionCommentLikeRepository.findByUserIdAndCommentId(userId, commentId)
                    .orElseThrow(() -> new BadRequestException("좋아요 데이터를 찾을 수 없습니다."));
            collectionCommentLikeRepository.remove(commentLike);
            
            return new LikeStatusResponse(false);
        } else {
            // 좋아요가 없으면 추가
            UserBirdCollectionCommentLike commentLike = new UserBirdCollectionCommentLike(user, comment);
            collectionCommentLikeRepository.save(commentLike);
            
            return new LikeStatusResponse(true);
        }
    }
}
