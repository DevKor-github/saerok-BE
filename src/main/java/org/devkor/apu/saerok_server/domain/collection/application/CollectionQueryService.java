package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionDetailResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionEditDataResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetNearbyCollectionsResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.MyCollectionsResponse;
import org.devkor.apu.saerok_server.domain.collection.application.dto.GetCollectionEditDataCommand;
import org.devkor.apu.saerok_server.domain.collection.application.dto.GetNearbyCollectionsCommand;
import org.devkor.apu.saerok_server.domain.collection.application.helper.CollectionImageUrlService;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionImage;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionImageRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionLikeRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.core.util.PointFactory;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionQueryService {
    
    private final CollectionRepository collectionRepository;
    private final CollectionImageRepository collectionImageRepository;
    private final CollectionLikeRepository collectionLikeRepository;
    private final CollectionCommentRepository collectionCommentRepository;
    private final CollectionWebMapper collectionWebMapper;
    private final UserRepository userRepository;
    private final UserProfileImageUrlService userProfileImageUrlService;
    private final CollectionImageUrlService collectionImageUrlService;

    public GetCollectionEditDataResponse getCollectionEditDataResponse(GetCollectionEditDataCommand command) {
        userRepository.findById(command.userId()).orElseThrow(() -> new BadRequestException("유효하지 않은 사용자 id예요"));
        UserBirdCollection collection = collectionRepository.findById(command.collectionId()).orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));
        if (!command.userId().equals(collection.getUser().getId())) {
            throw new ForbiddenException("해당 컬렉션에 대한 권한이 없어요");
        }

        GetCollectionEditDataResponse response = collectionWebMapper.toGetCollectionEditDataResponse(collection);
        Optional<String> imageUrl = collectionImageUrlService.getPrimaryImageUrlFor(collection);
        imageUrl.ifPresentOrElse(
                url -> {
                    Long imageId = collectionImageRepository.findByCollectionId(collection.getId()).stream()
                            .findFirst()
                            .map(UserBirdCollectionImage::getId)
                            .orElse(null);

                    response.setImageId(imageId);
                    response.setImageUrl(url);
                },
                () -> {
                    response.setImageId(null);
                    response.setImageUrl(null);
                }
        );

        return response;
    }

    public MyCollectionsResponse getMyCollections(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("유효하지 않은 사용자 id예요"));

        List<UserBirdCollection> collections = collectionRepository.findByUserId(userId);
        Map<Long, String> urlMap = collectionImageUrlService.getPrimaryImageUrlsFor(collections);
        Map<Long, String> thumbnailUrlMap = collectionImageUrlService.getPrimaryImageThumbnailUrlsFor(collections);

        List<MyCollectionsResponse.Item> items = collections.stream()
                .map(c -> {
                    String imageUrl = urlMap.get(c.getId());
                    String thumbnailUrl = thumbnailUrlMap.get(c.getId());
                    long likeCount = collectionLikeRepository.countByCollectionId(c.getId());
                    long commentCount = collectionCommentRepository.countByCollectionId(c.getId());
                    return new MyCollectionsResponse.Item(
                            c.getId(),
                            imageUrl,
                            thumbnailUrl,
                            c.getBird() == null ? null : c.getBird().getName().getKoreanName(),
                            likeCount,
                            commentCount,
                            c.getCreatedAt(),
                            c.getDiscoveredDate()
                    );
                })
                .toList();

        // TODO: 많은 쿼리로 인한 성능 이슈 우려됨. 나중에 개선해야 할지도

        return new MyCollectionsResponse(items);
    }

    public GetCollectionDetailResponse getCollectionDetailResponse(Long userId, Long collectionId) {

        if (userId != null) {
            userRepository.findById(userId).orElseThrow(() -> new NotFoundException("유효하지 않은 사용자 id예요"));
        }

        UserBirdCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));

        if (collection.getAccessLevel() == AccessLevelType.PRIVATE && (userId == null || !userId.equals(collection.getUser().getId()))) {
            throw new ForbiddenException("해당 컬렉션을 볼 수 있는 권한이 없어요");
        }

        String imageUrl = collectionImageUrlService.getPrimaryImageUrlFor(collection).orElse(null);
        long likeCount = collectionLikeRepository.countByCollectionId(collectionId);
        long commentCount = collectionCommentRepository.countByCollectionId(collectionId);
        boolean isLikedByMe = userId != null && collectionLikeRepository.existsByUserIdAndCollectionId(userId, collectionId);
        boolean isMine = userId != null && userId.equals(collection.getUser().getId());
        String userProfileImageUrl = userProfileImageUrlService.getProfileImageUrlFor(collection.getUser());

        return collectionWebMapper.toGetCollectionDetailResponse(collection, imageUrl, userProfileImageUrl, likeCount, commentCount, isLikedByMe, isMine);
    }

    public GetNearbyCollectionsResponse getNearbyCollections(GetNearbyCollectionsCommand command) {
        if (command.userId() != null) {
            userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("유효하지 않은 사용자 id예요"));
        }

        if (command.userId() == null && command.isMineOnly()) {
            throw new BadRequestException("비회원 사용자는 isMineOnly = false만 사용 가능해요.");
        }

        Point refPoint = PointFactory.create(command.latitude(), command.longitude());
        List<UserBirdCollection> collections = collectionRepository.findNearby(refPoint, command.radiusMeters(), command.userId(), command.isMineOnly());

        Map<Long, String> urlMap = collectionImageUrlService.getPrimaryImageUrlsFor(collections);
        Map<Long, String> thumbnailUrlMap = collectionImageUrlService.getPrimaryImageThumbnailUrlsFor(collections);

        List<GetNearbyCollectionsResponse.Item> items = collections.stream()
                .map(collection -> {
                    String imageUrl = urlMap.get(collection.getId());
                    String thumbnailUrl = thumbnailUrlMap.get(collection.getId());
                    long likeCount = collectionLikeRepository.countByCollectionId(collection.getId());
                    long commentCount = collectionCommentRepository.countByCollectionId(collection.getId());
                    boolean isLikedByMe = command.userId() != null && collectionLikeRepository.existsByUserIdAndCollectionId(command.userId(), collection.getId());
                    String userProfileImageUrl = userProfileImageUrlService.getProfileImageUrlFor(collection.getUser());

                    return collectionWebMapper.toGetNearbyCollectionsResponseItem(collection, imageUrl, thumbnailUrl, userProfileImageUrl, likeCount, commentCount, isLikedByMe);
                })
                .toList();
        // TODO: 많은 쿼리로 인한 성능 이슈 우려됨. 나중에 개선해야 할지도

        GetNearbyCollectionsResponse response = new GetNearbyCollectionsResponse();
        response.setItems(items);
        return response;
    }
}
