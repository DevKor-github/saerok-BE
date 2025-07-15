package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.LikeStatusResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionLike;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionLikeRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectionLikeCommandService {

    private final CollectionLikeRepository collectionLikeRepository;
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;

    /**
     * 좋아요 토글 (추가/제거)
     */
    public LikeStatusResponse toggleLikeResponse(Long userId, Long collectionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        
        UserBirdCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("컬렉션을 찾을 수 없습니다."));

        boolean exists = collectionLikeRepository.existsByUserIdAndCollectionId(userId, collectionId);

        if (exists) {
            // 좋아요가 이미 존재하면 제거
            UserBirdCollectionLike like = collectionLikeRepository.findByUserIdAndCollectionId(userId, collectionId)
                    .orElseThrow(() -> new BadRequestException("좋아요 데이터를 찾을 수 없습니다."));
            collectionLikeRepository.remove(like);
            
            return new LikeStatusResponse(false);
        } else {
            // 좋아요가 없으면 추가
            UserBirdCollectionLike like = new UserBirdCollectionLike(user, collection);
            collectionLikeRepository.save(like);
            
            return new LikeStatusResponse(true);
        }
    }
}
