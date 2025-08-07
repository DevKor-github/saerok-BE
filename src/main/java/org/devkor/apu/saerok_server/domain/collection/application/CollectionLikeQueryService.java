package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionLikersResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetLikedCollectionsResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.LikeStatusResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionLikeRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionLikeWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionLikeQueryService {

    private final CollectionLikeRepository collectionLikeRepository;
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final CollectionLikeWebMapper collectionLikeWebMapper;
    private final UserProfileImageUrlService userProfileImageUrlService;

    /**
     * 좋아요 상태 조회
     */
    public LikeStatusResponse getLikeStatusResponse(Long userId, Long collectionId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        collectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("컬렉션을 찾을 수 없습니다."));

        boolean isLiked = collectionLikeRepository.existsByUserIdAndCollectionId(userId, collectionId);
        return new LikeStatusResponse(isLiked);
    }

    /**
     * 사용자가 좋아요한 컬렉션 ID 목록 조회
     */
    public GetLikedCollectionsResponse getLikedCollectionIdsResponse(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        List<UserBirdCollection> collections = collectionLikeRepository.findLikedCollectionsByUserId(userId);
        return collectionLikeWebMapper.toGetLikedCollectionsResponse(collections);
    }

    /**
     * 컬렉션을 좋아요한 사용자 목록 조회
     */
    public GetCollectionLikersResponse getCollectionLikersResponse(Long collectionId) {
        collectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("컬렉션을 찾을 수 없습니다."));

        List<User> users = collectionLikeRepository.findLikersByCollectionId(collectionId);
        Map<Long, String> profileImageUrls = userProfileImageUrlService.getProfileImageUrlsFor(users);
        return collectionLikeWebMapper.toGetCollectionLikersResponse(users, profileImageUrls);
    }
}
