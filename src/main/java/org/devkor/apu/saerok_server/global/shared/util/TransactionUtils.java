package org.devkor.apu.saerok_server.global.shared.util;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionUtils {

    private TransactionUtils() {}

    /**
     * 주어진 작업을 현재 트랜잭션 커밋 직후(afterCommit)에 실행하거나,
     * 활성화된 트랜잭션이 없으면 즉시 실행합니다.
     *
     * <p>트랜잭션이 존재하는 경우, 커밋이 정상적으로 완료된 이후에만 작업이 실행되며
     * 롤백 시에는 실행되지 않습니다. 외부 시스템 연동(S3 삭제, 이메일 발송 등)
     * 처럼 DB 변경이 확정된 뒤에 실행해야 하는 작업에 사용합니다.</p>
     *
     * @param task 커밋 이후(또는 즉시) 실행할 Runnable 작업
     */
    public static void runAfterCommitOrNow(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run(); // 트랜잭션 없을 때 즉시 실행
        }
    }
}
