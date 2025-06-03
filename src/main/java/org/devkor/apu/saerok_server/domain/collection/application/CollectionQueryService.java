package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionDetailResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionEditDataResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.MyCollectionsResponse;
import org.devkor.apu.saerok_server.domain.collection.application.dto.GetCollectionEditDataCommand;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionImage;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionImageRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.util.CloudFrontUrlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionQueryService {
    
    private final CollectionRepository collectionRepository;
    private final CollectionImageRepository collectionImageRepository;
    private final CollectionWebMapper collectionWebMapper;
    private final UserRepository userRepository;
    private final CloudFrontUrlService cloudFrontUrlService;

    public GetCollectionEditDataResponse getCollectionEditDataResponse(GetCollectionEditDataCommand command) {
        userRepository.findById(command.userId()).orElseThrow(() -> new BadRequestException("유효하지 않은 사용자 id예요"));
        UserBirdCollection collection = collectionRepository.findById(command.collectionId()).orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));
        if (!command.userId().equals(collection.getUser().getId())) {
            throw new ForbiddenException("해당 컬렉션에 대한 권한이 없어요");
        }

        GetCollectionEditDataResponse response = collectionWebMapper.toGetCollectionEditDataResponse(collection);
        List<UserBirdCollectionImage> images = collectionImageRepository.findByCollectionId(command.collectionId());
        List<GetCollectionEditDataResponse.ImageInfo> imageInfos = images.stream()
                .map(image -> new GetCollectionEditDataResponse.ImageInfo(
                            image.getId(),
                            cloudFrontUrlService.toImageUrl(image.getObjectKey())
                    ))
                .toList();
        response.setImages(imageInfos);
        return response;
    }

    public List<MyCollectionsResponse> getMyCollectionsResponse(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("유효하지 않은 사용자 id예요"));
        List<UserBirdCollection> collections = collectionRepository.findByUserId(userId);
        List<MyCollectionsResponse> result = new ArrayList<>();

        for (UserBirdCollection collection : collections) {
            List<String> objectKeys = collectionImageRepository.findObjectKeysByCollectionId(collection.getId());
            String imageUrl = objectKeys.isEmpty() ? null : cloudFrontUrlService.toImageUrl(objectKeys.getFirst());

            MyCollectionsResponse response = new MyCollectionsResponse();
            response.setCollectionId(collection.getId());
            response.setImageUrl(imageUrl);
            response.setBirdName(collection.getBirdKoreanName());
            result.add(response);
        }

        return result;
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
        String imageUrl = objectKeys.isEmpty() ? null : cloudFrontUrlService.toImageUrl(objectKeys.getFirst());

        return collectionWebMapper.toGetCollectionDetailResponse(collection, imageUrl);
    }
}
