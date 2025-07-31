package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionDetailResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionEditDataResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetNearbyCollectionsResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.MyCollectionsResponse;
import org.devkor.apu.saerok_server.domain.collection.application.dto.GetCollectionEditDataCommand;
import org.devkor.apu.saerok_server.domain.collection.application.dto.GetNearbyCollectionsCommand;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionImageRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionLikeRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.core.util.PointFactory;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.util.ImageDomainService;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private final UserProfileImageRepository userProfileImageRepository;
    private final ImageDomainService imageDomainService;

    public GetCollectionEditDataResponse getCollectionEditDataResponse(GetCollectionEditDataCommand command) {
        userRepository.findById(command.userId()).orElseThrow(() -> new BadRequestException("유효하지 않은 사용자 id예요"));
        UserBirdCollection collection = collectionRepository.findById(command.collectionId()).orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));
        if (!command.userId().equals(collection.getUser().getId())) {
            throw new ForbiddenException("해당 컬렉션에 대한 권한이 없어요");
        }

        GetCollectionEditDataResponse response = collectionWebMapper.toGetCollectionEditDataResponse(collection);
        collectionImageRepository.findByCollectionId(command.collectionId())
                .stream()
                .findFirst()
                .ifPresentOrElse(
                        image -> {
                            response.setImageId(image.getId());
                            response.setImageUrl(imageDomainService.toUploadImageUrl(image.getObjectKey()));
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

        List<MyCollectionsResponse.Item> items = collectionRepository.findByUserId(userId).stream()
                .map(collection -> {
                    String objectKey = collectionImageRepository.findObjectKeysByCollectionId(collection.getId()).stream()
                            .findFirst().orElse(null);
                    String imageUrl = objectKey != null ? imageDomainService.toUploadImageUrl(objectKey) : null;
                    
                    // 좋아요 수 조회
                    long likeCount = collectionLikeRepository.countByCollectionId(collection.getId());
                    
                    // 댓글 수 조회
                    long commentCount = collectionCommentRepository.countByCollectionId(collection.getId());

                    return new MyCollectionsResponse.Item(
                            collection.getId(),
                            imageUrl,
                            collection.getBird() == null ? null : collection.getBird().getName().getKoreanName(),
                            likeCount,
                            commentCount
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

        List<String> objectKeys = collectionImageRepository.findObjectKeysByCollectionId(collectionId);
        String imageUrl = objectKeys.isEmpty() ? null : imageDomainService.toUploadImageUrl(objectKeys.getFirst());
        
        // 좋아요 수 조회
        long likeCount = collectionLikeRepository.countByCollectionId(collectionId);
        
        // 댓글 수 조회
        long commentCount = collectionCommentRepository.countByCollectionId(collectionId);
        
        // 내가 좋아요 눌렀는지 확인 (비회원인 경우 false)
        boolean isLiked = userId != null && collectionLikeRepository.existsByUserIdAndCollectionId(userId, collectionId);

        // 사용자 프로필 이미지 URL 조회
        String userProfileImageUrl = imageDomainService.toUploadImageUrl(
            userProfileImageRepository.findObjectKeyByUserId(collection.getUser().getId())
        );

        return collectionWebMapper.toGetCollectionDetailResponse(collection, imageUrl, userProfileImageUrl, likeCount, commentCount, isLiked);
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

        List<GetNearbyCollectionsResponse.Item> items = collections.stream()
                .map(collection -> {
                    List<String> objectKeys = collectionImageRepository.findObjectKeysByCollectionId(collection.getId());
                    String imageUrl = objectKeys.isEmpty() ? null : imageDomainService.toUploadImageUrl(objectKeys.getFirst());
                    
                    // 좋아요 수 조회
                    long likeCount = collectionLikeRepository.countByCollectionId(collection.getId());
                    
                    // 댓글 수 조회
                    long commentCount = collectionCommentRepository.countByCollectionId(collection.getId());
                    
                    // 내가 좋아요 눌렀는지 확인 (비회원인 경우 false)
                    boolean isLiked = command.userId() != null && collectionLikeRepository.existsByUserIdAndCollectionId(command.userId(), collection.getId());
                    
                    return collectionWebMapper.toGetNearbyCollectionsResponseItem(collection, imageUrl, likeCount, commentCount, isLiked);
                })
                .toList();
        // TODO: 많은 쿼리로 인한 성능 이슈 우려됨. 나중에 개선해야 할지도

        GetNearbyCollectionsResponse response = new GetNearbyCollectionsResponse();
        response.setItems(items);
        return response;
    }
}
