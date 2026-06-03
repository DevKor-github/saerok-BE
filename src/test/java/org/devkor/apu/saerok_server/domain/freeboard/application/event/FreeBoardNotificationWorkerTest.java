package org.devkor.apu.saerok_server.domain.freeboard.application.event;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class FreeBoardNotificationWorkerTest {

    @Mock private NotificationPublisher publisher;

    private FreeBoardNotificationWorker worker;

    @BeforeEach
    void setUp() {
        TargetMetadataPort metadataPort = (target, baseExtras) -> {
            Map<String, Object> extras = baseExtras == null ? new HashMap<>() : new HashMap<>(baseExtras);

            if (target.type() == TargetType.FREE_BOARD_POST) {
                extras.put("freeBoardPostId", target.id());
            } else if (target.type() == TargetType.FREE_BOARD_COMMENT) {
                extras.put("freeBoardCommentId", target.id());
                extras.put("freeBoardPostId", 100L);
            }
            return extras;
        };

        worker = new FreeBoardNotificationWorker(new NotifyActionDsl(publisher, metadataPort));
    }

    @Test
    @DisplayName("원댓글 알림은 게시글 소유자에게 하나 생성된다")
    void handle_topLevelComment_generatesPostOwnerNotification() {
        worker.handle(new FreeBoardNotificationEvent.CommentCreated(
                1L, "commenter",
                100L, 2L,
                null, null,
                "hello"
        ));

        ArgumentCaptor<NotificationPayload> payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(publisher).push(payloadCaptor.capture());

        ActionNotificationPayload payload = (ActionNotificationPayload) payloadCaptor.getValue();
        assertThat(payload.recipientId()).isEqualTo(2L);
        assertThat(payload.subject()).isEqualTo(NotificationSubject.FREE_BOARD_POST);
        assertThat(payload.action()).isEqualTo(NotificationAction.COMMENT);
        assertThat(payload.type()).isEqualTo(NotificationType.COMMENTED_ON_FREE_BOARD_POST);
        assertThat(payload.relatedId()).isEqualTo(100L);
        assertThat(payload.extras()).containsEntry("freeBoardPostId", 100L);
        assertThat(payload.extras()).containsEntry("comment", "hello");
    }

    @Test
    @DisplayName("자기 게시글에 원댓글을 달면 알림이 생성되지 않는다")
    void handle_selfComment_skipsNotifications() {
        worker.handle(new FreeBoardNotificationEvent.CommentCreated(
                1L, "owner",
                100L, 1L,
                null, null,
                "self comment"
        ));

        verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("대댓글 알림은 원댓글 작성자와 게시글 소유자에게 각각 생성된다")
    void handle_replyComment_generatesTwoNotifications() {
        worker.handle(new FreeBoardNotificationEvent.CommentCreated(
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
                        org.assertj.core.groups.Tuple.tuple(2L, NotificationType.REPLIED_TO_FREE_BOARD_COMMENT),
                        org.assertj.core.groups.Tuple.tuple(3L, NotificationType.COMMENTED_ON_FREE_BOARD_POST)
                );

        ActionNotificationPayload replyPayload = payloads.stream()
                .filter(p -> p.type() == NotificationType.REPLIED_TO_FREE_BOARD_COMMENT)
                .findFirst()
                .orElseThrow();

        assertThat(replyPayload.subject()).isEqualTo(NotificationSubject.FREE_BOARD_COMMENT);
        assertThat(replyPayload.action()).isEqualTo(NotificationAction.REPLY);
        assertThat(replyPayload.relatedId()).isEqualTo(100L);
        assertThat(replyPayload.extras()).containsEntry("freeBoardCommentId", 200L);
        assertThat(replyPayload.extras()).containsEntry("freeBoardPostId", 100L);
        assertThat(replyPayload.extras()).containsEntry("comment", "reply body");
    }

    @Test
    @DisplayName("대댓글: 게시글 소유자가 원댓글 작성자와 같으면 게시글 소유자 알림은 중복으로 생성하지 않는다")
    void handle_replyWhenPostOwnerIsParentAuthor_generatesOnlyReplyNotification() {
        worker.handle(new FreeBoardNotificationEvent.CommentCreated(
                1L, "replier",
                100L, 2L,
                200L, 2L,
                "reply body"
        ));

        ArgumentCaptor<NotificationPayload> payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(publisher).push(payloadCaptor.capture());

        ActionNotificationPayload payload = (ActionNotificationPayload) payloadCaptor.getValue();
        assertThat(payload.recipientId()).isEqualTo(2L);
        assertThat(payload.type()).isEqualTo(NotificationType.REPLIED_TO_FREE_BOARD_COMMENT);
    }

    @Test
    @DisplayName("대댓글: 본인이 자기 댓글에 자답해도 게시글 소유자에게는 알림이 간다")
    void handle_selfReplyOnOwnComment_stillNotifiesPostOwner() {
        worker.handle(new FreeBoardNotificationEvent.CommentCreated(
                1L, "self",
                100L, 3L,
                200L, 1L,
                "self reply"
        ));

        ArgumentCaptor<NotificationPayload> payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(publisher).push(payloadCaptor.capture());

        ActionNotificationPayload payload = (ActionNotificationPayload) payloadCaptor.getValue();
        assertThat(payload.recipientId()).isEqualTo(3L);
        assertThat(payload.type()).isEqualTo(NotificationType.COMMENTED_ON_FREE_BOARD_POST);
    }

    @Test
    @DisplayName("발송 중 예외가 나도 워커는 예외를 외부로 전파하지 않는다")
    void handle_publisherFailure_swallowsException() {
        doThrow(new IllegalStateException("push failed")).when(publisher).push(org.mockito.ArgumentMatchers.any());

        assertThatCode(() -> worker.handle(new FreeBoardNotificationEvent.CommentCreated(
                1L, "commenter",
                100L, 2L,
                null, null,
                "boom"
        ))).doesNotThrowAnyException();
    }
}
