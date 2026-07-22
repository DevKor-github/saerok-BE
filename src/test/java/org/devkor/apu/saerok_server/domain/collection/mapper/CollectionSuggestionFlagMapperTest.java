package org.devkor.apu.saerok_server.domain.collection.mapper;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionDetailResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetLikedCollectionsResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetNearbyCollectionsResponse;
import org.devkor.apu.saerok_server.domain.collection.application.helper.CollectionImageUrlService;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.util.PointFactory;
import org.devkor.apu.saerok_server.domain.profile.api.dto.response.UserProfileResponse;
import org.devkor.apu.saerok_server.domain.profile.mapper.UserProfileMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class CollectionSuggestionFlagMapperTest {

    private final CollectionWebMapper collectionWebMapper = Mappers.getMapper(CollectionWebMapper.class);
    private final CollectionLikeWebMapper collectionLikeWebMapper = Mappers.getMapper(CollectionLikeWebMapper.class);
    private final UserProfileMapper userProfileMapper = Mappers.getMapper(UserProfileMapper.class);

    @Test
    void collectionResponsesMapCanSuggestBirdIdFromCollectionState() {
        UserBirdCollection collection = suggestionEnabledCollection();

        GetCollectionDetailResponse detail = collectionWebMapper.toGetCollectionDetailResponse(
                collection, null, null, null, 0, 0, false, true
        );
        GetNearbyCollectionsResponse.Item nearby = collectionWebMapper.toGetNearbyCollectionsResponseItem(
                collection, null, null, null, null, 0, 0, false, true
        );
        GetLikedCollectionsResponse.Item liked = collectionLikeWebMapper.toLikedCollectionItem(collection);

        assertThat(detail.getCanSuggestBirdId()).isTrue();
        assertThat(nearby.getCanSuggestBirdId()).isTrue();
        assertThat(liked.canSuggestBirdId()).isTrue();
    }

    @Test
    void profileCollectionItemsMapCanSuggestBirdIdFromCollectionState() {
        UserBirdCollection collection = suggestionEnabledCollection();
        CollectionImageUrlService imageUrlService = mock(CollectionImageUrlService.class);
        given(imageUrlService.getPrimaryImageUrlsFor(List.of(collection))).willReturn(Map.of());
        given(imageUrlService.getPrimaryImageThumbnailUrlsFor(List.of(collection))).willReturn(Map.of());

        List<UserProfileResponse.CollectionItem> items =
                userProfileMapper.toCollectionItems(List.of(collection), imageUrlService);

        assertThat(items).singleElement()
                .extracting(UserProfileResponse.CollectionItem::canSuggestBirdId)
                .isEqualTo(true);
    }

    private UserBirdCollection suggestionEnabledCollection() {
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 10L);

        UserBirdCollection collection = UserBirdCollection.builder()
                .user(owner)
                .bird(null)
                .discoveredDate(LocalDate.of(2026, 7, 22))
                .location(PointFactory.create(37.5, 127.0))
                .accessLevel(AccessLevelType.PUBLIC)
                .birdIdSuggestionEnabled(true)
                .build();
        ReflectionTestUtils.setField(collection, "id", 1L);
        ReflectionTestUtils.setField(collection, "createdAt", OffsetDateTime.now());
        return collection;
    }
}
