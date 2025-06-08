package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionDetailResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionImageRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.core.util.PointFactory;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.util.CloudFrontUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CollectionQueryServiceTest {

    CollectionQueryService collectionQueryService;

    @Mock CollectionRepository collectionRepository;
    @Mock CollectionImageRepository collectionImageRepository;
    @Mock CollectionWebMapper collectionWebMapper;
    @Mock UserRepository userRepository;
    @Mock CloudFrontUrlService cloudFrontUrlService;

    Field userIdField;
    Field collectionIdField;
    Field collectionUserField;

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        collectionQueryService = new CollectionQueryService(
                collectionRepository,
                collectionImageRepository,
                collectionWebMapper,
                userRepository,
                cloudFrontUrlService
        );

        userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);

        collectionIdField = UserBirdCollection.class.getDeclaredField("id");
        collectionIdField.setAccessible(true);

        collectionUserField = UserBirdCollection.class.getDeclaredField("user");
        collectionUserField.setAccessible(true);
    }

    @Test
    @DisplayName("공개 컬렉션은 익명 사용자도 조회 가능")
    void getCollectionDetail_public_anonymous() throws IllegalAccessException {
        // given
        Long collectionId = 1L;
        User owner = new User();
        userIdField.set(owner, 10L);

        UserBirdCollection collection = new UserBirdCollection();
        collectionIdField.set(collection, collectionId);
        collectionUserField.set(collection, owner);
        collection.setAccessLevel(AccessLevelType.PUBLIC);

        String objectKey = "collection-images/42/15fd9a32-bb4e-4b7c-bd8b-4fd1e2b3d8a4";
        String imageUrl = "https://cdn.example.com/collection-images/42/15fd9a32-bb4e-4b7c-bd8b-4fd1e2b3d8a4";

        GetCollectionDetailResponse expected = new GetCollectionDetailResponse();

        given(collectionRepository.findById(collectionId)).willReturn(Optional.of(collection));
        given(collectionImageRepository.findObjectKeysByCollectionId(collectionId)).willReturn(List.of(objectKey));
        given(cloudFrontUrlService.toImageUrl(objectKey)).willReturn(imageUrl);
        given(collectionWebMapper.toGetCollectionDetailResponse(collection, imageUrl)).willReturn(expected);

        // when
        GetCollectionDetailResponse actual = collectionQueryService.getCollectionDetailResponse(null, collectionId);

        // then
        assertSame(expected, actual);
        verifyNoInteractions(userRepository);        // 익명 호출에서는 userRepository가 불리지 않는다
    }

    @Test
    @DisplayName("비공개 컬렉션 - 본인 조회 가능")
    void getCollectionDetail_private_owner() throws IllegalAccessException {
        // given
        Long userId = 1L;
        Long collectionId = 2L;

        User owner = new User();
        userIdField.set(owner, userId);

        UserBirdCollection collection = new UserBirdCollection();
        collectionIdField.set(collection, collectionId);
        collectionUserField.set(collection, owner);
        collection.setAccessLevel(AccessLevelType.PRIVATE);

        given(userRepository.findById(userId)).willReturn(Optional.of(owner));
        given(collectionRepository.findById(collectionId)).willReturn(Optional.of(collection));
        given(collectionImageRepository.findObjectKeysByCollectionId(collectionId)).willReturn(List.of());
        GetCollectionDetailResponse expected = new GetCollectionDetailResponse();
        given(collectionWebMapper.toGetCollectionDetailResponse(collection, null)).willReturn(expected);

        // when
        GetCollectionDetailResponse actual =
                collectionQueryService.getCollectionDetailResponse(userId, collectionId);

        // then
        assertSame(expected, actual);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {99L})
    @DisplayName("비공개 컬렉션 - 소유자가 아니거나 익명 사용자는 ForbiddenException")
    void getCollectionDetail_private_forbidden(Long requesterId) throws IllegalAccessException {
        // given
        Long collectionId = 3L;

        User owner = new User();
        userIdField.set(owner, 10L);

        UserBirdCollection collection = new UserBirdCollection();
        collectionIdField.set(collection, collectionId);
        collectionUserField.set(collection, owner);
        collection.setAccessLevel(AccessLevelType.PRIVATE);

        if (requesterId != null) {
            User requester = new User();
            userIdField.set(requester, requesterId);
            given(userRepository.findById(requesterId)).willReturn(Optional.of(requester));
        }

        given(collectionRepository.findById(collectionId)).willReturn(Optional.of(collection));

        // when / then
        assertThrows(ForbiddenException.class,
                () -> collectionQueryService.getCollectionDetailResponse(requesterId, collectionId));
    }

    @Test
    @DisplayName("존재하지 않는 컬렉션 id면 NotFoundException")
    void getCollectionDetail_collectionNotFound() {
        // given
        Long collectionId = 404L;
        given(collectionRepository.findById(collectionId)).willReturn(Optional.empty());

        // when / then
        assertThrows(NotFoundException.class,
                () -> collectionQueryService.getCollectionDetailResponse(null, collectionId));
    }

    @Test
    @DisplayName("userId가 유효하지 않으면 NotFoundException")
    void getCollectionDetail_userNotFound() {
        // given
        Long badUserId = 888L;
        Long collectionId = 5L;

        given(userRepository.findById(badUserId)).willReturn(Optional.empty());

        // when / then
        assertThrows(NotFoundException.class,
                () -> collectionQueryService.getCollectionDetailResponse(badUserId, collectionId));
    }
}