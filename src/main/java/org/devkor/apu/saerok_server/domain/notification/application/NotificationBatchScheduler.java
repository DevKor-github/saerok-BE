package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.facade.NotificationPublisher;
import org.devkor.apu.saerok_server.domain.notification.application.model.batch.NotificationBatch;
import org.devkor.apu.saerok_server.global.core.config.feature.NotificationBatchConfig;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 알림 배치 스케줄러.
 * 주기적으로 만료된 배치를 조회하여 전송한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationBatchScheduler {

    private final NotificationBatchService batchService;
    private final NotificationPublisher publisher;
    private final NotificationBatchConfig batchConfig;

    /**
     * 10초마다 만료된 배치를 조회하여 전송.
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    public void processExpiredBatches() {
        if (!batchConfig.isEnabled()) {
            return;
        }

        try {
            List<NotificationBatch> expiredBatches = batchService.findExpiredBatches();

            if (expiredBatches.isEmpty()) {
                return;
            }

            for (NotificationBatch batch : expiredBatches) {
                try {
                    publisher.pushBatch(batch);
                } catch (Exception e) {
                    log.error("만료된 배치 처리에 실패했습니다: key={}", batch.getKey(), e);
                }
            }

        } catch (Exception e) {
            log.error("Error in batch scheduler", e);
        }
    }
}
