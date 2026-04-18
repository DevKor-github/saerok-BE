package org.devkor.apu.saerok_server.domain.admin.notification.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.facade.NotifySystemService;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminNotificationWorker {

    private final NotifySystemService notifySystemService;

    @Async("pushNotificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AdminNotificationEvent.AdminMessageSent event) {
        try {
            Map<String, Object> extras = Map.of(
                    "title", event.title(),
                    "body", event.body()
            );
            notifySystemService.notifyUsersDeduplicatedPush(
                    event.recipientIds(),
                    NotificationType.SYSTEM_ADMIN_MESSAGE,
                    null,
                    extras
            );
        } catch (Exception e) {
            log.error("Failed to send admin message notification: recipientCount={}",
                    event.recipientIds().size(), e);
        }
    }

    @Async("pushNotificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AdminNotificationEvent.ContentDeletedByReport event) {
        try {
            Map<String, Object> extras = Map.of("reason", event.reason());
            notifySystemService.notifyUser(
                    event.contentOwnerId(),
                    NotificationType.SYSTEM_CONTENT_DELETED,
                    null,
                    extras
            );
        } catch (Exception e) {
            log.error("Failed to send content-deleted notification: ownerId={}",
                    event.contentOwnerId(), e);
        }
    }
}
