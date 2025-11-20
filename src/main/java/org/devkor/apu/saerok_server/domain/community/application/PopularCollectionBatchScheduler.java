package org.devkor.apu.saerok_server.domain.community.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularCollectionBatchScheduler {

    private final PopularCollectionBatchService popularCollectionBatchService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void refreshPopularCollections() {
        try {
            popularCollectionBatchService.refreshPopularCollections();
        } catch (Exception e) {
            log.error("[popular] failed to refresh popular collections", e);
        }
    }
}
