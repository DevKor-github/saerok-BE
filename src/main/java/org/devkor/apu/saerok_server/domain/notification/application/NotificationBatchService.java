package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.batch.*;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.store.NotificationBatchStore;
import org.devkor.apu.saerok_server.global.core.config.feature.NotificationBatchConfig;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 알림 배치 관리 서비스.
 * 배치 생성, 추가, 조회 등의 작업을 담당한다.
 */
@Service
@RequiredArgsConstructor
public class NotificationBatchService {

    private final NotificationBatchStore batchStore;
    private final NotificationBatchConfig batchConfig;

    /**
     * 배치에 알림 추가.
     * 기존 배치가 있으면 추가하고 최대 시간까지 연장하며, 없으면 새로 생성한다.
     */
    public BatchResult addToBatch(ActionNotificationPayload payload) {
        // 배치 처리가 비활성화되어 있으면 즉시 전송
        if (!batchConfig.isEnabled()) {
            return BatchResult.sendImmediately();
        }

        BatchKey key = new BatchKey(
                payload.recipientId(),
                payload.subject(),
                payload.action(),
                payload.relatedId()
        );

        BatchActor actor = BatchActor.of(payload.actorId(), payload.actorName());

        synchronized (this.getLockKey(key)) {
            Optional<NotificationBatch> existingBatch = batchStore.findBatch(key);

            if (existingBatch.isPresent()) {
                // 기존 배치에 추가하고 최대 시간까지 연장
                NotificationBatch updatedBatch = existingBatch.get()
                        .addActor(actor, payload.extras(), batchConfig.getMaxWindowSeconds());

                batchStore.saveBatch(updatedBatch);

                return BatchResult.added(updatedBatch);

            } else {
                // 새 배치 생성
                NotificationBatch newBatch = NotificationBatch.create(
                        key,
                        actor,
                        batchConfig.getInitialWindowSeconds(),
                        payload.extras()
                );

                batchStore.saveBatch(newBatch);

                return BatchResult.created(newBatch);
            }
        }
    }

    public List<NotificationBatch> findExpiredBatches() {
        return batchStore.findExpiredBatches();
    }

    public void deleteBatch(BatchKey key) {
        batchStore.deleteBatch(key);
    }

    /**
     * 동시성 제어를 위한 락 키 생성.
     * 같은 배치 키에 대한 동시 접근을 막기 위해 String 인터닝 활용.
     */
    private String getLockKey(BatchKey key) {
        return key.toRedisKey().intern();
    }
}
