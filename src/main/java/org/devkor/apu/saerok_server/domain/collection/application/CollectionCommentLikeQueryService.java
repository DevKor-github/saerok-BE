package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.LikeStatusResponse;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentLikeRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionCommentLikeQueryService {

    private final CollectionCommentLikeRepository collectionCommentLikeRepository;
    private final CollectionCommentRepository collectionCommentRepository;
    private final UserRepository userRepository;

    /**
     * 댓글 좋아요 상태 조회
     */
    public LikeStatusResponse getLikeStatusResponse(Long userId, Long commentId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        collectionCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));

        boolean isLiked = collectionCommentLikeRepository.existsByUserIdAndCommentId(userId, commentId);
        return new LikeStatusResponse(isLiked);
    }
}
