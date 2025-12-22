package org.devkor.apu.saerok_server.domain.notification.application.model.batch;

/**
 * 배치 내 행동 주체(actor) 정보.
 */
public record BatchActor(
        Long id,
        String name
) {
    public static BatchActor of(Long id, String name) {
        return new BatchActor(id, name);
    }
}
