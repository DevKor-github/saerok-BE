package org.devkor.apu.saerok_server.domain.collection.mapper;

import org.devkor.apu.saerok_server.domain.collection.api.dto.request.CreateCollectionImageRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.request.CreateCollectionRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.request.UpdateCollectionRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.*;
import org.devkor.apu.saerok_server.domain.collection.application.dto.CreateCollectionCommand;
import org.devkor.apu.saerok_server.domain.collection.application.dto.CreateCollectionImageCommand;
import org.devkor.apu.saerok_server.domain.collection.application.dto.DeleteCollectionCommand;
import org.devkor.apu.saerok_server.domain.collection.application.dto.*;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface CollectionWebMapper {

    @Mapping(target = "userId", source = "userId")
    CreateCollectionCommand toCreateCollectionCommand(CreateCollectionRequest request, Long userId);

    @Mapping(target = "collectionId", source = "collectionId")
    CreateCollectionResponse toCreateCollectionResponse(Long collectionId);

    CreateCollectionImageCommand toCreateCollectionImageCommand(CreateCollectionImageRequest request);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "collectionId", source = "collectionId")
    DeleteCollectionCommand toDeleteCollectionCommand(Long userId, Long collectionId);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "collectionId", source = "collectionId")
    GetCollectionEditDataCommand toGetCollectionDataCommand(Long userId, Long collectionId);

    @Mapping(target = "birdId", source = "bird.id")
    GetCollectionEditDataResponse toGetCollectionEditDataResponse(UserBirdCollection collection);

    @Mapping(target = "userId", source = "userId")
    UpdateCollectionCommand toUpdateCollectionCommand(UpdateCollectionRequest request, Long userId, Long collectionId);

    @Mapping(target = "birdId", source = "collection.bird.id")
    @Mapping(target = "collectionId", source = "collection.id")
    UpdateCollectionResponse toUpdateCollectionResponse(UserBirdCollection collection, String imageUrl);

    @Mapping(target = "bird.birdId", source = "collection", qualifiedByName = "getBirdId")
    @Mapping(target = "bird.koreanName", source = "collection", qualifiedByName = "getBirdKoreanName")
    @Mapping(target = "bird.scientificName", source = "collection", qualifiedByName = "getBirdScientificName")
    @Mapping(target = "collectionId", source = "collection.id")
    @Mapping(target = "user.userId", source = "collection.user.id")
    GetCollectionDetailResponse toGetCollectionDetailResponse(UserBirdCollection collection, String imageUrl);

    @Mapping(target = "collectionId", source = "collection.id")
    @Mapping(target = "koreanName", source = "collection.bird.name.koreanName")
    GetNearbyCollectionsResponse.Item toGetNearbyCollectionsResponseItem(UserBirdCollection collection, String imageUrl);

    @Named("getBirdId")
    default Long getBirdId(UserBirdCollection collection) {
        return collection.getBird() == null ? null : collection.getBird().getId();
    }

    @Named("getBirdKoreanName")
    default String getBirdKoreanName(UserBirdCollection collection) {
        return collection.getBird() == null ? null : collection.getBird().getName().getKoreanName();
    }

    @Named("getBirdScientificName")
    default String getBirdScientificName(UserBirdCollection collection) {
        return collection.getBird() == null ? null : collection.getBird().getName().getScientificName();
    }
}
