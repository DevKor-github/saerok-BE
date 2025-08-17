package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.LikeStatusResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionLike;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionLikeRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.notification.application.facade.NotificationPublisher;
import org.devkor.apu.saerok_server.domain.notification.application.facade.NotifyActionDsl;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.TargetType;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionLikeCommandServiceTest {

    CollectionLikeCommandService collectionLikeCommandService;

    @Mock CollectionLikeRepository collectionLikeRepository;
    @Mock CollectionRepository collectionRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationPublisher publisher;

    @BeforeEach
    void setUp() {
        NotifyActionDsl notifyActionDsl = new NotifyActionDsl(
                publisher,
                (target, base) -> {
                    Map<String,Object> extras = base == null ? new HashMap<>() : new HashMap<>(base);
                    if (target.type() == TargetType.COLLECTION) {
                        extras.put("collectionId", target.id());
                        extras.put("collectionImageUrl", null);
                    }
                    return extras;
                }
        );
        collectionLikeCommandService = new CollectionLikeCommandService(
                collectionLikeRepository, collectionRepository, userRepository, notifyActionDsl
        );
    }

    @Test
    @DisplayName("좋아요 토글 - 좋아요가 없으면 추가하고 알림 발송")
    void toggleLike_addLike_success() {
        Long userId = 1L;
        Long collectionId = 2L;

        User user = new User();
        User collectionOwner = new User();
        UserBirdCollection collection = new UserBirdCollection();

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(collectionOwner, "id", 999L);
        ReflectionTestUtils.setField(collection, "user", collectionOwner);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(collectionRepository.findById(collectionId)).willReturn(Optional.of(collection));
        given(collectionLikeRepository.existsByUserIdAndCollectionId(userId, collectionId)).willReturn(false);

        LikeStatusResponse response = collectionLikeCommandService.toggleLikeResponse(userId, collectionId);

        assertTrue(response.isLiked());
        verify(collectionLikeRepository).existsByUserIdAndCollectionId(userId, collectionId);

        ArgumentCaptor<NotificationPayload> payloadCap = ArgumentCaptor.forClass(NotificationPayload.class);
        ArgumentCaptor<Target> targetCap = ArgumentCaptor.forClass(Target.class);
        verify(publisher).push(payloadCap.capture(), targetCap.capture());

        ActionNotificationPayload p = (ActionNotificationPayload) payloadCap.getValue();
        assertEquals(NotificationSubject.COLLECTION, p.subject());
        assertEquals(NotificationAction.LIKE, p.action());
        assertEquals(999L, p.recipientId());
        assertEquals(userId, p.actorId());
        Map<String, Object> extras = p.extras();
        assertEquals(collectionId, extras.get("collectionId"));
        assertTrue(extras.containsKey("collectionImageUrl"));
        assertEquals(Target.collection(collectionId), targetCap.getValue());
    }

    @Test
    @DisplayName("좋아요 토글 - 이미 좋아요가 있으면 제거 (알림 없음)")
    void toggleLike_removeLike_success() {
        Long userId = 1L;
        Long collectionId = 2L;

        User user = new User();
        UserBirdCollection collection = new UserBirdCollection();
        UserBirdCollectionLike existingLike = new UserBirdCollectionLike(user, collection);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(collectionRepository.findById(collectionId)).willReturn(Optional.of(collection));
        given(collectionLikeRepository.existsByUserIdAndCollectionId(userId, collectionId)).willReturn(true);
        given(collectionLikeRepository.findByUserIdAndCollectionId(userId, collectionId)).willReturn(Optional.of(existingLike));

        LikeStatusResponse response = collectionLikeCommandService.toggleLikeResponse(userId, collectionId);

        assertFalse(response.isLiked());
        verify(collectionLikeRepository).existsByUserIdAndCollectionId(userId, collectionId);
        verify(collectionLikeRepository).findByUserIdAndCollectionId(userId, collectionId);
        verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("좋아요 토글 - 사용자가 존재하지 않으면 예외")
    void toggleLike_userNotFound_throwsException() {
        Long userId = 999L;
        Long collectionId = 2L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> collectionLikeCommandService.toggleLikeResponse(userId, collectionId));
    }

    @Test
    @DisplayName("좋아요 토글 - 컬렉션이 존재하지 않으면 예외")
    void toggleLike_collectionNotFound_throwsException() {
        Long userId = 1L;
        Long collectionId = 999L;

        User user = new User();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(collectionRepository.findById(collectionId)).willReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> collectionLikeCommandService.toggleLikeResponse(userId, collectionId));
    }
}
