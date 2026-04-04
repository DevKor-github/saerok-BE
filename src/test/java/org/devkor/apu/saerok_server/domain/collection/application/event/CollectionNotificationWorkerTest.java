package org.devkor.apu.saerok_server.domain.collection.application.event;

import org.devkor.apu.saerok_server.domain.notification.application.facade.NotificationPublisher;
import org.devkor.apu.saerok_server.domain.notification.application.facade.NotifyActionDsl;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.TargetType;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.port.TargetMetadataPort;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CollectionNotificationWorkerTest {

    @Mock private NotificationPublisher publisher;

    private CollectionNotificationWorker worker;

    @BeforeEach
    void setUp() {
        TargetMetadataPort metadataPort = (target, baseExtras) -> {
            Map<String, Object> extras = baseExtras == null ? new HashMap<>() : new HashMap<>(baseExtras);

            if (target.type() == TargetType.COLLECTION) {
                extras.put("collectionId", target.id());
                extras.put("collectionImageUrl", "https://example.com/collections/" + target.id() + ".webp");
            } else {
                extras.put("commentId", target.id());
                extras.put("collectionId", 999L);
                extras.put("collectionImageUrl", "https://example.com/comments/" + target.id() + ".webp");
            }
            return extras;
        };

        worker = new CollectionNotificationWorker(new NotifyActionDsl(publisher, metadataPort));
    }

    @Test
    @DisplayName("대댓글 알림은 원댓글 작성자와 컬렉션 소유자에게 각각 생성된다")
    void handle_replyComment_generatesTwoNotifications() {
        worker.handle(new CollectionNotificationEvent.CommentCreated(
                1L, "replier",
                100L, 3L,
                200L, 2L,
                "reply body"
        ));

        ArgumentCaptor<NotificationPayload> payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(publisher, times(2)).push(payloadCaptor.capture());

        List<ActionNotificationPayload> payloads = payloadCaptor.getAllValues().stream()
                .map(ActionNotificationPayload.class::cast)
                .toList();

        assertThat(payloads)
                .extracting(ActionNotificationPayload::recipientId, ActionNotificationPayload::type)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(2L, NotificationType.REPLIED_TO_COMMENT),
                        org.assertj.core.groups.Tuple.tuple(3L, NotificationType.COMMENTED_ON_COLLECTION)
                );

        ActionNotificationPayload replyPayload = payloads.stream()
                .filter(payload -> payload.type() == NotificationType.REPLIED_TO_COMMENT)
                .findFirst()
                .orElseThrow();

        assertThat(replyPayload.subject()).isEqualTo(NotificationSubject.COMMENT);
        assertThat(replyPayload.action()).isEqualTo(NotificationAction.REPLY);
        assertThat(replyPayload.relatedId()).isEqualTo(999L);
        assertThat(replyPayload.extras()).containsEntry("commentId", 200L);
        assertThat(replyPayload.extras()).containsEntry("collectionId", 999L);
        assertThat(replyPayload.extras()).containsEntry("comment", "reply body");
    }

    @Test
    @DisplayName("자기 컬렉션 원댓글은 알림을 생성하지 않는다")
    void handle_selfComment_skipsNotifications() {
        worker.handle(new CollectionNotificationEvent.CommentCreated(
                1L, "owner",
                100L, 1L,
                null, null,
                "self comment"
        ));

        verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("좋아요 알림은 컬렉션 좋아요 payload 하나를 생성한다")
    void handle_collectionLiked_generatesNotification() {
        worker.handle(new CollectionNotificationEvent.CollectionLiked(
                1L, "liker",
                100L, 2L
        ));

        ArgumentCaptor<NotificationPayload> payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(publisher).push(payloadCaptor.capture());

        ActionNotificationPayload payload = (ActionNotificationPayload) payloadCaptor.getValue();
        assertThat(payload.recipientId()).isEqualTo(2L);
        assertThat(payload.subject()).isEqualTo(NotificationSubject.COLLECTION);
        assertThat(payload.action()).isEqualTo(NotificationAction.LIKE);
        assertThat(payload.type()).isEqualTo(NotificationType.LIKED_ON_COLLECTION);
        assertThat(payload.relatedId()).isEqualTo(100L);
        assertThat(payload.extras()).containsEntry("collectionId", 100L);
    }

    @Test
    @DisplayName("동정 제안 알림은 제안된 새 이름을 포함한 payload를 생성한다")
    void handle_birdIdSuggested_generatesNotification() {
        worker.handle(new CollectionNotificationEvent.BirdIdSuggested(
                1L, "suggester",
                100L, 2L,
                "직박구리"
        ));

        ArgumentCaptor<NotificationPayload> payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(publisher).push(payloadCaptor.capture());

        ActionNotificationPayload payload = (ActionNotificationPayload) payloadCaptor.getValue();
        assertThat(payload.recipientId()).isEqualTo(2L);
        assertThat(payload.type()).isEqualTo(NotificationType.SUGGESTED_BIRD_ID_ON_COLLECTION);
        assertThat(payload.extras()).containsEntry("collectionId", 100L);
        assertThat(payload.extras()).containsEntry("suggestedName", "직박구리");
    }

    @Test
    @DisplayName("발송 중 예외가 나도 워커는 예외를 외부로 전파하지 않는다")
    void handle_likeFailure_swallowsException() {
        doThrow(new IllegalStateException("push failed")).when(publisher).push(org.mockito.ArgumentMatchers.any());

        assertThatCode(() -> worker.handle(new CollectionNotificationEvent.CollectionLiked(
                1L, "liker",
                100L, 2L
        ))).doesNotThrowAnyException();
    }
}
