package org.devkor.apu.saerok_server.domain.admin.notification.application.event;

import org.devkor.apu.saerok_server.domain.notification.application.facade.NotifySystemService;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminNotificationWorkerTest {

    @Mock private NotifySystemService notifySystemService;

    private AdminNotificationWorker worker;

    @BeforeEach
    void setUp() {
        worker = new AdminNotificationWorker(notifySystemService);
    }

    @Test
    @DisplayName("관리자 메시지 이벤트 처리 시 notifyUsersDeduplicatedPush를 올바르게 호출한다")
    void handle_adminMessageSent_callsNotifyUsersDeduplicatedPush() {
        List<Long> recipientIds = List.of(1L, 2L, 3L);
        var event = new AdminNotificationEvent.AdminMessageSent(recipientIds, "안내 사항", "서비스 점검 예정입니다.");

        worker.handle(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> extrasCaptor = ArgumentCaptor.forClass(Map.class);

        verify(notifySystemService).notifyUsersDeduplicatedPush(
                idsCaptor.capture(),
                eq(NotificationType.SYSTEM_ADMIN_MESSAGE),
                isNull(),
                extrasCaptor.capture()
        );

        assertThat(idsCaptor.getValue()).containsExactly(1L, 2L, 3L);
        assertThat(extrasCaptor.getValue())
                .containsEntry("title", "안내 사항")
                .containsEntry("body", "서비스 점검 예정입니다.");
    }

    @Test
    @DisplayName("콘텐츠 삭제 이벤트 처리 시 notifyUser를 올바르게 호출한다")
    void handle_contentDeletedByReport_callsNotifyUser() {
        var event = new AdminNotificationEvent.ContentDeletedByReport(42L, "커뮤니티 가이드라인 위반");

        worker.handle(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> extrasCaptor = ArgumentCaptor.forClass(Map.class);

        verify(notifySystemService).notifyUser(
                eq(42L),
                eq(NotificationType.SYSTEM_CONTENT_DELETED),
                isNull(),
                extrasCaptor.capture()
        );

        assertThat(extrasCaptor.getValue()).containsEntry("reason", "커뮤니티 가이드라인 위반");
    }

    @Test
    @DisplayName("관리자 메시지 발송 중 예외가 나도 외부로 전파하지 않는다")
    void handle_adminMessageSentFailure_swallowsException() {
        doThrow(new RuntimeException("push failed"))
                .when(notifySystemService).notifyUsersDeduplicatedPush(any(), any(), any(), any());

        assertThatCode(() -> worker.handle(
                new AdminNotificationEvent.AdminMessageSent(List.of(1L), "제목", "내용")
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("콘텐츠 삭제 알림 발송 중 예외가 나도 외부로 전파하지 않는다")
    void handle_contentDeletedFailure_swallowsException() {
        doThrow(new RuntimeException("push failed"))
                .when(notifySystemService).notifyUser(any(), any(), any(), any());

        assertThatCode(() -> worker.handle(
                new AdminNotificationEvent.ContentDeletedByReport(1L, "사유")
        )).doesNotThrowAnyException();
    }
}
