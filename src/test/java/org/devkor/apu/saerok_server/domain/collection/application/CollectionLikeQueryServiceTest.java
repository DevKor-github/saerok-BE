package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionLikersResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetLikedCollectionsResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.LikeStatusResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionLikeRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionLikeWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CollectionLikeQueryServiceTest {

    CollectionLikeQueryService collectionLikeQueryService;

    @Mock CollectionLikeRepository      collectionLikeRepository;
    @Mock CollectionRepository          collectionRepository;
    @Mock UserRepository                userRepository;
    @Mock CollectionLikeWebMapper       collectionLikeWebMapper;
    @Mock UserProfileImageUrlService    userProfileImageUrlService;

    private User testUser;
    private UserBirdCollection testCollection;

    @BeforeEach
    void setUp() {
        collectionLikeQueryService = new CollectionLikeQueryService(
                collectionLikeRepository,
                collectionRepository,
                userRepository,
                collectionLikeWebMapper,
                userProfileImageUrlService
        );

        testUser = new User();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testCollection = new UserBirdCollection();
        ReflectionTestUtils.setField(testCollection, "id", 1L);
    }

    @Test
    @DisplayName("좋아요 상태 조회 - 좋아요 있음")
    void getLikeStatusResponse_liked_returnsTrue() {
        Long userId = 1L;
        Long collectionId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(collectionRepository.findById(collectionId)).willReturn(Optional.of(testCollection));
        given(collectionLikeRepository.existsByUserIdAndCollectionId(userId, collectionId)).willReturn(true);

        LikeStatusResponse result = collectionLikeQueryService.getLikeStatusResponse(userId, collectionId);

        assertTrue(result.isLiked());
        verify(userRepository).findById(userId);
        verify(collectionRepository).findById(collectionId);
        verify(collectionLikeRepository).existsByUserIdAndCollectionId(userId, collectionId);
    }

    @Test
    @DisplayName("좋아요 상태 조회 - 좋아요 없음")
    void getLikeStatusResponse_notLiked_returnsFalse() {
        Long userId = 1L;
        Long collectionId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(collectionRepository.findById(collectionId)).willReturn(Optional.of(testCollection));
        given(collectionLikeRepository.existsByUserIdAndCollectionId(userId, collectionId)).willReturn(false);

        LikeStatusResponse result = collectionLikeQueryService.getLikeStatusResponse(userId, collectionId);

        assertFalse(result.isLiked());
        verify(userRepository).findById(userId);
        verify(collectionRepository).findById(collectionId);
        verify(collectionLikeRepository).existsByUserIdAndCollectionId(userId, collectionId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 좋아요 상태 조회 시 예외 발생")
    void getLikeStatusResponse_userNotFound_throwsException() {
        Long userId = 999L;
        Long collectionId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                collectionLikeQueryService.getLikeStatusResponse(userId, collectionId)
        );

        assertTrue(exception.getMessage().contains("사용자를 찾을 수 없습니다"));
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("존재하지 않는 컬렉션으로 좋아요 상태 조회 시 예외 발생")
    void getLikeStatusResponse_collectionNotFound_throwsException() {
        Long userId = 1L;
        Long collectionId = 999L;

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(collectionRepository.findById(collectionId)).willReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                collectionLikeQueryService.getLikeStatusResponse(userId, collectionId)
        );

        assertTrue(exception.getMessage().contains("컬렉션을 찾을 수 없습니다"));
        verify(userRepository).findById(userId);
        verify(collectionRepository).findById(collectionId);
    }

    @Test
    @DisplayName("사용자가 좋아요한 컬렉션 목록 조회")
    void getLikedCollectionIdsResponse_returnsLikedCollections() {
        Long userId = 1L;
        List<UserBirdCollection> likedCollections = List.of(testCollection);
        GetLikedCollectionsResponse expectedResponse = new GetLikedCollectionsResponse(List.of());

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(collectionLikeRepository.findLikedCollectionsByUserId(userId)).willReturn(likedCollections);
        given(collectionLikeWebMapper.toGetLikedCollectionsResponse(likedCollections)).willReturn(expectedResponse);

        GetLikedCollectionsResponse result = collectionLikeQueryService.getLikedCollectionIdsResponse(userId);

        assertEquals(expectedResponse, result);
        verify(userRepository).findById(userId);
        verify(collectionLikeRepository).findLikedCollectionsByUserId(userId);
        verify(collectionLikeWebMapper).toGetLikedCollectionsResponse(likedCollections);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 좋아요한 컬렉션 목록 조회 시 예외 발생")
    void getLikedCollectionIdsResponse_userNotFound_throwsException() {
        Long userId = 999L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                collectionLikeQueryService.getLikedCollectionIdsResponse(userId)
        );

        assertTrue(exception.getMessage().contains("사용자를 찾을 수 없습니다"));
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("컬렉션을 좋아요한 사용자 목록 조회")
    void getCollectionLikersResponse_returnsLikers() {
        Long collectionId = 1L;
        List<User> likers = List.of(testUser);
        Map<Long, String> profileImageUrls = Map.of(1L, "https://example.com/profile/1.jpg");
        GetCollectionLikersResponse expectedResponse = new GetCollectionLikersResponse(List.of());

        given(collectionRepository.findById(collectionId)).willReturn(Optional.of(testCollection));
        given(collectionLikeRepository.findLikersByCollectionId(collectionId)).willReturn(likers);
        given(userProfileImageUrlService.getProfileImageUrlsFor(likers))
                .willReturn(profileImageUrls);
        given(collectionLikeWebMapper.toGetCollectionLikersResponse(likers, profileImageUrls))
                .willReturn(expectedResponse);

        GetCollectionLikersResponse result = collectionLikeQueryService.getCollectionLikersResponse(collectionId);

        assertEquals(expectedResponse, result);
        verify(collectionRepository).findById(collectionId);
        verify(collectionLikeRepository).findLikersByCollectionId(collectionId);
        verify(userProfileImageUrlService).getProfileImageUrlsFor(likers);
        verify(collectionLikeWebMapper).toGetCollectionLikersResponse(likers, profileImageUrls);
    }

    @Test
    @DisplayName("존재하지 않는 컬렉션으로 좋아요한 사용자 목록 조회 시 예외 발생")
    void getCollectionLikersResponse_collectionNotFound_throwsException() {
        Long collectionId = 999L;

        given(collectionRepository.findById(collectionId)).willReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                collectionLikeQueryService.getCollectionLikersResponse(collectionId)
        );

        assertTrue(exception.getMessage().contains("컬렉션을 찾을 수 없습니다"));
        verify(collectionRepository).findById(collectionId);
    }
}
