package org.devkor.apu.saerok_server.domain.notification.application.store;

import org.devkor.apu.saerok_server.domain.notification.application.model.batch.BatchKey;
import org.devkor.apu.saerok_server.domain.notification.application.model.batch.NotificationBatch;

import java.util.List;
import java.util.Optional;

/**
 * 알림 배치 저장소 인터페이스.
 */
public interface NotificationBatchStore {

    Optional<NotificationBatch> findBatch(BatchKey key);

    void saveBatch(NotificationBatch batch);

    void deleteBatch(BatchKey key);

    /**
     * 만료된 배치 목록 조회.
     * redis의 key 만료를 감안한 메서드
     */
    List<NotificationBatch> findExpiredBatches();
}
