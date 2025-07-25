package org.devkor.apu.saerok_server.domain.collection.mapper;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionLikersResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetLikedCollectionsResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface CollectionLikeWebMapper {

    // 좋아요한 컬렉션 ID 목록 조회
    default GetLikedCollectionsResponse toGetLikedCollectionsResponse(List<UserBirdCollection> collections) {
        if (collections == null || collections.isEmpty()) {
            return new GetLikedCollectionsResponse(List.of());
        }
        
        List<GetLikedCollectionsResponse.Item> items = toLikedCollectionItems(collections);
        return new GetLikedCollectionsResponse(items);
    }

    List<GetLikedCollectionsResponse.Item> toLikedCollectionItems(List<UserBirdCollection> collections);

    @Mapping(target = "collectionId", source = "id")
    GetLikedCollectionsResponse.Item toLikedCollectionItem(UserBirdCollection collection);

    // 컬렉션을 좋아요한 사용자 목록 조회
    default GetCollectionLikersResponse toGetCollectionLikersResponse(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new GetCollectionLikersResponse(List.of());
        }
        
        List<GetCollectionLikersResponse.Item> items = toCollectionLikerItems(users);
        return new GetCollectionLikersResponse(items);
    }

    List<GetCollectionLikersResponse.Item> toCollectionLikerItems(List<User> users);

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "nickname", source = "nickname")
    GetCollectionLikersResponse.Item toCollectionLikerItem(User user);

}
