package org.devkor.apu.saerok_server.domain.community.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.application.helper.CollectionImageUrlService;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionLikeRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.BirdIdSuggestionRepository;
import org.devkor.apu.saerok_server.domain.community.core.repository.PopularCollectionRepository;
import org.devkor.apu.saerok_server.domain.community.api.dto.common.CommunityCollectionInfo;
import org.devkor.apu.saerok_server.domain.community.api.dto.common.CommunityUserInfo;
import org.devkor.apu.saerok_server.domain.community.mapper.CommunityWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommunityDataAssembler {

    private final CollectionImageUrlService collectionImageUrlService;
    private final UserProfileImageUrlService userProfileImageUrlService;
    private final CollectionLikeRepository collectionLikeRepository;
    private final CollectionCommentRepository collectionCommentRepository;
    private final PopularCollectionRepository popularCollectionRepository;
    private final CommunityWebMapper communityWebMapper;
    private final BirdIdSuggestionRepository birdIdSuggestionRepository;

    public List<CommunityCollectionInfo> toCollectionInfos(List<UserBirdCollection> collections, Long userId) {
        if (collections.isEmpty()) {
            return List.of();
        }

        Map<Long, String> imageUrls = collectionImageUrlService.getPrimaryImageUrlsFor(collections);

        List<Long> collectionIds = collections.stream()
                .map(UserBirdCollection::getId)
                .toList();
        Map<Long, Boolean> popularStatusMap = popularCollectionRepository.existsByCollectionIds(collectionIds);

        List<Long> pendingCollectionIds = collections.stream()
                .filter(c -> c.getBird() == null)
                .map(UserBirdCollection::getId)
                .toList();
        Map<Long, Long> suggestionUserCounts = pendingCollectionIds.isEmpty() 
                ? Map.of() 
                : birdIdSuggestionRepository.countDistinctUsersByCollectionIds(pendingCollectionIds);

        return collections.stream()
                .map(collection -> {
                    String imageUrl = imageUrls.get(collection.getId());
                    String userProfileImageUrl = userProfileImageUrlService.getProfileImageUrlFor(collection.getUser());
                    long likeCount = collectionLikeRepository.countByCollectionId(collection.getId());
                    long commentCount = collectionCommentRepository.countByCollectionId(collection.getId());
                    boolean isLiked = userId != null && collectionLikeRepository.existsByUserIdAndCollectionId(userId, collection.getId());
                    boolean isPopular = popularStatusMap.getOrDefault(collection.getId(), false);

                    Long suggestionUserCount = collection.getBird() == null 
                            ? suggestionUserCounts.getOrDefault(collection.getId(), 0L) 
                            : null;
                    
                    return communityWebMapper.toCommunityCollectionInfo(
                            collection, imageUrl, userProfileImageUrl, likeCount, commentCount, isLiked, isPopular, suggestionUserCount
                    );
                })
                .toList();
    }

    public List<CommunityUserInfo> toUserInfos(List<User> users) {
        return users.stream()
                .map(user -> {
                    String profileImageUrl = userProfileImageUrlService.getProfileImageUrlFor(user);
                    return communityWebMapper.toCommunityUserInfo(user, profileImageUrl);
                })
                .toList();
    }
}
