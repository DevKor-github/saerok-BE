package org.devkor.apu.saerok_server.domain.notification.application.model.batch;

/**
 * 배치 추가 작업의 결과.
 */
public record BatchResult(
        BatchAction action,
        NotificationBatch batch
) {

    public static BatchResult created(NotificationBatch batch) { return new BatchResult(BatchAction.CREATED, batch); }

    public static BatchResult added(NotificationBatch batch) {
        return new BatchResult(BatchAction.ADDED, batch);
    }

    public static BatchResult sendImmediately() {
        return new BatchResult(BatchAction.SEND_IMMEDIATELY, null);
    }

    public boolean shouldSendImmediately() {
        return action == BatchAction.SEND_IMMEDIATELY;
    }

    public enum BatchAction {
        CREATED,            // 새 배치 생성됨
        ADDED,              // 기존 배치에 추가됨
        SEND_IMMEDIATELY    // 즉시 전송 (배치 처리 안 함)
    }
}
