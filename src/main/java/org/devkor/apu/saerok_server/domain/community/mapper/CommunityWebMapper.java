package org.devkor.apu.saerok_server.domain.community.mapper;

import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.community.api.dto.common.CommunityCollectionInfo;
import org.devkor.apu.saerok_server.domain.community.api.dto.common.CommunityUserInfo;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = OffsetDateTimeLocalizer.class)
public interface CommunityWebMapper {

    @Mapping(target = "collectionId", source = "collection.id")
    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "thumbnailImageUrl", source = "thumbnailImageUrl")
    @Mapping(target = "discoveredDate", source = "collection.discoveredDate")
    @Mapping(target = "createdAt", expression = "java(OffsetDateTimeLocalizer.toSeoulLocalDateTime(collection.getCreatedAt()))")
    @Mapping(target = "latitude", source = "collection.latitude")
    @Mapping(target = "longitude", source = "collection.longitude")
    @Mapping(target = "locationAlias", source = "collection.locationAlias")
    @Mapping(target = "address", source = "collection.address")
    @Mapping(target = "note", source = "collection.note")
    @Mapping(target = "likeCount", source = "likeCount")
    @Mapping(target = "commentCount", source = "commentCount")
    @Mapping(target = "isLiked", source = "isLiked")
    @Mapping(target = "isPopular", source = "isPopular")
    @Mapping(target = "suggestionUserCount", source = "suggestionUserCount")
    @Mapping(target = "bird", expression = "java(mapBirdInfo(collection))")
    @Mapping(target = "user", expression = "java(mapUserInfo(collection, userProfileImageUrl, thumbnailProfileImageUrl))")
    CommunityCollectionInfo toCommunityCollectionInfo(
            UserBirdCollection collection,
            String imageUrl,
            String thumbnailImageUrl,
            String userProfileImageUrl,
            String thumbnailProfileImageUrl,
            Long likeCount,
            Long commentCount,
            Boolean isLiked,
            Boolean isPopular,
            Long suggestionUserCount
    );

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "nickname", source = "user.nickname")
    @Mapping(target = "profileImageUrl", source = "profileImageUrl")
    @Mapping(target = "thumbnailProfileImageUrl", source = "thumbnailProfileImageUrl")
    CommunityUserInfo toCommunityUserInfo(User user, String profileImageUrl, String thumbnailProfileImageUrl);

    default CommunityCollectionInfo.BirdInfo mapBirdInfo(UserBirdCollection collection) {
        if (collection.getBird() == null) {
            return null;
        }
        return new CommunityCollectionInfo.BirdInfo(
                collection.getBird().getId(),
                collection.getBird().getName() != null ? collection.getBird().getName().getKoreanName() : null
        );
    }

    default CommunityCollectionInfo.UserInfo mapUserInfo(UserBirdCollection collection, String profileImageUrl, String thumbnailProfileImageUrl) {
        if (collection.getUser() == null) {
            return null;
        }
        return new CommunityCollectionInfo.UserInfo(
                collection.getUser().getId(),
                collection.getUser().getNickname(),
                profileImageUrl,
                thumbnailProfileImageUrl
        );
    }
}
