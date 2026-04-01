package org.devkor.apu.saerok_server.domain.announcement.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.announcement.application.AnnouncementBulkNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnnouncementNotificationWorker {

    private final AnnouncementBulkNotificationService bulkNotificationService;

    @Async("announcementNotificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AnnouncementPublishedEvent event) {
        try {
            bulkNotificationService.sendBulkNotification(event.announcementId());
        } catch (Exception e) {
            log.error("Failed to send bulk announcement notification: announcementId={}",
                    event.announcementId(), e);
        }
    }
}
