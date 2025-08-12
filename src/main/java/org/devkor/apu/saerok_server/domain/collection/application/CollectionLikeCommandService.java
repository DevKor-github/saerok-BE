package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.LikeStatusResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionLike;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionLikeRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.notification.application.PushNotificationService;
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
    private final PushNotificationService pushNotificationService;

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
            
            // 자신의 컬렉션이 아닌 경우에만 푸시 알림 발송
            if (!collection.getUser().getId().equals(userId)) {
                pushNotificationService.sendCollectionLikeNotification(
                    collection.getUser().getId(), // 컬렉션 소유자에게
                    userId, // 좋아요를 누른 사용자 ID
                    collectionId // 컬렉션 ID
                );
            }
            
            return new LikeStatusResponse(true);
        }
    }
}
