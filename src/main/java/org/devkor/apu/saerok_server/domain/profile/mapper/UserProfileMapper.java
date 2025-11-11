package org.devkor.apu.saerok_server.domain.profile.mapper;

import org.devkor.apu.saerok_server.domain.collection.application.helper.CollectionImageUrlService;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.profile.api.dto.response.UserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        imports = OffsetDateTimeLocalizer.class)
public interface UserProfileMapper {

    /* 메인 매핑 */
    @Mapping(target = "nickname",        source = "user.nickname")
    @Mapping(target = "joinedDate",
            expression = "java(OffsetDateTimeLocalizer.toSeoulLocalDate(user.getJoinedAt()))")
    @Mapping(target = "profileImageUrl",
            expression = "java(userProfileImageUrlService.getProfileImageUrlFor(user))")
    @Mapping(target = "thumbnailProfileImageUrl",
            expression = "java(userProfileImageUrlService.getProfileThumbnailImageUrlFor(user))")
    @Mapping(target = "collectionCount",
            expression = "java((long) collections.size())")
    @Mapping(target = "collections",
            expression = "java(toCollectionItems(collections, collectionImageUrlService))")
    UserProfileResponse toResponse(
            User user,
            List<UserBirdCollection> collections,
            @Context UserProfileImageUrlService userProfileImageUrlService,
            @Context CollectionImageUrlService  collectionImageUrlService
    );

    /* 하위 컬렉션 매핑은 default 메서드로 수동 구현 */
    default List<UserProfileResponse.CollectionItem> toCollectionItems(
            List<UserBirdCollection> collections,
            @Context CollectionImageUrlService collectionImageUrlService
    ) {
        Map<Long, String> imageUrlMap =
                collectionImageUrlService.getPrimaryImageUrlsFor(collections);
        Map<Long, String> thumbnailUrlMap =
                collectionImageUrlService.getPrimaryImageThumbnailUrlsFor(collections);

        return collections.stream()
                .map(c -> new UserProfileResponse.CollectionItem(
                        c.getId(),
                        c.getBird() == null ? null : c.getBird().getId(),
                        c.getBird() == null ? null : c.getBird().getName().getKoreanName(),
                        c.getBird() == null ? null : c.getBird().getName().getScientificName(),
                        imageUrlMap.get(c.getId()),
                        thumbnailUrlMap.get(c.getId()),
                        c.getNote(),
                        c.getDiscoveredDate(),
                        OffsetDateTimeLocalizer.toSeoulLocalDate(c.getCreatedAt())
                ))
                .collect(Collectors.toList());
    }
}
