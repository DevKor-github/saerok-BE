package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionDetailResponse;
import org.devkor.apu.saerok_server.domain.collection.application.helper.CollectionImageUrlService;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.*;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CollectionQueryServiceTest {

    CollectionQueryService collectionQueryService;

    /* ────────────────Mocks─────────────── */
    @Mock CollectionRepository        collectionRepository;
    @Mock CollectionImageRepository   collectionImageRepository;   // (id 조회용으로만 주입)
    @Mock CollectionLikeRepository    collectionLikeRepository;
    @Mock CollectionCommentRepository collectionCommentRepository;
    @Mock CollectionWebMapper         collectionWebMapper;
    @Mock UserRepository              userRepository;
    @Mock UserProfileImageUrlService  userProfileImageUrlService;
    @Mock CollectionImageUrlService   collectionImageUrlService;
    /* ──────────────────────────────────── */

    /* 리플렉션으로 PK·연관 세팅하기 위한 Field 캐시 */
    Field userIdField;
    Field collectionIdField;
    Field collectionUserField;

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        collectionQueryService = new CollectionQueryService(
                collectionRepository,
                collectionImageRepository,
                collectionLikeRepository,
                collectionCommentRepository,
                collectionWebMapper,
                userRepository,
                userProfileImageUrlService,
                collectionImageUrlService
        );

        userIdField       = User.class.getDeclaredField("id");
        collectionIdField = UserBirdCollection.class.getDeclaredField("id");
        collectionUserField = UserBirdCollection.class.getDeclaredField("user");
        userIdField.setAccessible(true);
        collectionIdField.setAccessible(true);
        collectionUserField.setAccessible(true);
    }

    /* ───────────────────── 테스트 1 ───────────────────── */

    @Test
    @DisplayName("공개 컬렉션은 익명 사용자도 조회 가능")
    void getCollectionDetail_public_anonymous() throws IllegalAccessException {
        //-- Given
        Long collectionId = 1L;

        User owner = new User();
        userIdField.set(owner, 10L);

        UserBirdCollection collection = new UserBirdCollection();
        collectionIdField.set(collection, collectionId);
        collectionUserField.set(collection, owner);
        collection.setAccessLevel(AccessLevelType.PUBLIC);

        String imageUrl   = "https://cdn.example.com/collection-images/42/uuid";
        String profileUrl = "https://cdn.example.com/profile/10/profile.jpg";
        GetCollectionDetailResponse expected = new GetCollectionDetailResponse();

        given(collectionRepository.findById(collectionId))
                .willReturn(Optional.of(collection));
        given(collectionImageUrlService.getPrimaryImageUrlFor(collection))
                .willReturn(Optional.of(imageUrl));
        given(collectionLikeRepository.countByCollectionId(collectionId))
                .willReturn(5L);
        given(collectionCommentRepository.countByCollectionId(collectionId))
                .willReturn(3L);
        given(userProfileImageUrlService.getProfileImageUrlFor(owner))
                .willReturn(profileUrl);
        given(userProfileImageUrlService.getProfileThumbnailImageUrlFor(owner))
                .willReturn(profileUrl + "_thumb");
        given(collectionWebMapper.toGetCollectionDetailResponse(
                collection, imageUrl, profileUrl, profileUrl + "_thumb", 5L, 3L, false, false))
                .willReturn(expected);

        //-- When
        GetCollectionDetailResponse actual =
                collectionQueryService.getCollectionDetailResponse(null, collectionId);

        //-- Then
        assertSame(expected, actual);
        verifyNoInteractions(userRepository);    // 익명 사용자는 userRepository 조회 안 함
    }

    /* ───────────────────── 테스트 2 ───────────────────── */

    @Test
    @DisplayName("비공개 컬렉션 - 본인 조회 가능")
    void getCollectionDetail_private_owner() throws IllegalAccessException {
        //-- Given
        Long userId = 1L;
        Long collectionId = 2L;

        User owner = new User();
        userIdField.set(owner, userId);

        UserBirdCollection collection = new UserBirdCollection();
        collectionIdField.set(collection, collectionId);
        collectionUserField.set(collection, owner);
        collection.setAccessLevel(AccessLevelType.PRIVATE);

        String profileUrl = "https://cdn.example.com/profile/1/profile.jpg";
        GetCollectionDetailResponse expected = new GetCollectionDetailResponse();

        given(userRepository.findById(userId)).willReturn(Optional.of(owner));
        given(collectionRepository.findById(collectionId)).willReturn(Optional.of(collection));
        given(collectionImageUrlService.getPrimaryImageUrlFor(collection)).willReturn(Optional.empty());   // 이미지 없음
        given(collectionLikeRepository.countByCollectionId(collectionId)).willReturn(3L);
        given(collectionCommentRepository.countByCollectionId(collectionId)).willReturn(2L);
        given(collectionLikeRepository.existsByUserIdAndCollectionId(userId, collectionId))
                .willReturn(true);
        given(userProfileImageUrlService.getProfileImageUrlFor(owner)).willReturn(profileUrl);
        given(userProfileImageUrlService.getProfileThumbnailImageUrlFor(owner)).willReturn(profileUrl + "_thumb");
        given(collectionWebMapper.toGetCollectionDetailResponse(
                collection, null, profileUrl, profileUrl + "_thumb", 3L, 2L, true, true))
                .willReturn(expected);

        //-- When
        GetCollectionDetailResponse actual =
                collectionQueryService.getCollectionDetailResponse(userId, collectionId);

        //-- Then
        assertSame(expected, actual);
    }

    /* ───────────────────── 테스트 3 (Forbidden) ───────────────────── */

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = 99L)
    @DisplayName("비공개 컬렉션 - 소유자가 아니거나 익명 사용자는 ForbiddenException")
    void getCollectionDetail_private_forbidden(Long requesterId) throws IllegalAccessException {
        //-- Given
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

        //-- Expect / Then
        assertThrows(ForbiddenException.class,
                () -> collectionQueryService.getCollectionDetailResponse(requesterId, collectionId));
    }

    /* ───────────────────── 테스트 4 (컬렉션 없음) ───────────────────── */

    @Test
    @DisplayName("존재하지 않는 컬렉션 id면 NotFoundException")
    void getCollectionDetail_collectionNotFound() {
        Long collectionId = 404L;
        given(collectionRepository.findById(collectionId)).willReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> collectionQueryService.getCollectionDetailResponse(null, collectionId));
    }

    /* ───────────────────── 테스트 5 (사용자 없음) ───────────────────── */

    @Test
    @DisplayName("userId가 유효하지 않으면 NotFoundException")
    void getCollectionDetail_userNotFound() {
        Long badUserId = 888L;
        Long collectionId = 5L;

        given(userRepository.findById(badUserId)).willReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> collectionQueryService.getCollectionDetailResponse(badUserId, collectionId));
    }
}
