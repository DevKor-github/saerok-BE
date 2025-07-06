package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.LikeStatusResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionLike;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionLikeRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionLikeCommandServiceTest {

    CollectionLikeCommandService collectionLikeCommandService;

    @Mock CollectionLikeRepository collectionLikeRepository;
    @Mock CollectionRepository collectionRepository;
    @Mock UserRepository userRepository;

    @BeforeEach
    void setUp() {
        collectionLikeCommandService = new CollectionLikeCommandService(
                collectionLikeRepository,
                collectionRepository,
                userRepository
        );
    }

    @Test
    @DisplayName("좋아요 토글 - 좋아요가 없으면 추가")
    void toggleLike_addLike_success() {
        // given
        Long userId = 1L;
        Long collectionId = 2L;
        
        User user = new User();
        UserBirdCollection collection = new UserBirdCollection();
        
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(collectionRepository.findById(collectionId)).willReturn(Optional.of(collection));
        given(collectionLikeRepository.existsByUserIdAndCollectionId(userId, collectionId)).willReturn(false);

        // when
        LikeStatusResponse response = collectionLikeCommandService.toggleLikeResponse(userId, collectionId);

        // then
        assertTrue(response.isLiked()); // 비즈니스 로직 결과 검증
        verify(collectionLikeRepository).existsByUserIdAndCollectionId(userId, collectionId);
    }

    @Test
    @DisplayName("좋아요 토글 - 이미 좋아요가 있으면 제거")
    void toggleLike_removeLike_success() {
        // given
        Long userId = 1L;
        Long collectionId = 2L;
        
        User user = new User();
        UserBirdCollection collection = new UserBirdCollection();
        UserBirdCollectionLike existingLike = new UserBirdCollectionLike(user, collection);
        
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(collectionRepository.findById(collectionId)).willReturn(Optional.of(collection));
        given(collectionLikeRepository.existsByUserIdAndCollectionId(userId, collectionId)).willReturn(true);
        given(collectionLikeRepository.findByUserIdAndCollectionId(userId, collectionId))
                .willReturn(Optional.of(existingLike));

        // when
        LikeStatusResponse response = collectionLikeCommandService.toggleLikeResponse(userId, collectionId);

        // then
        assertFalse(response.isLiked()); // 비즈니스 로직 결과 검증
        verify(collectionLikeRepository).existsByUserIdAndCollectionId(userId, collectionId);
        verify(collectionLikeRepository).findByUserIdAndCollectionId(userId, collectionId);
    }

    @Test
    @DisplayName("좋아요 토글 - 사용자가 존재하지 않으면 예외")
    void toggleLike_userNotFound_throwsException() {
        // given
        Long userId = 999L;
        Long collectionId = 2L;
        
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class,
                () -> collectionLikeCommandService.toggleLikeResponse(userId, collectionId));
    }

    @Test
    @DisplayName("좋아요 토글 - 컬렉션이 존재하지 않으면 예외")
    void toggleLike_collectionNotFound_throwsException() {
        // given
        Long userId = 1L;
        Long collectionId = 999L;
        
        User user = new User();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(collectionRepository.findById(collectionId)).willReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class,
                () -> collectionLikeCommandService.toggleLikeResponse(userId, collectionId));
    }
}
