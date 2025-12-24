package org.devkor.apu.saerok_server.domain.notification.application.model.batch;

import lombok.Getter;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 알림 배치 도메인 모델.
 * 일정 시간 동안 같은 대상(수신자, 주제, 행동, 관련 ID)에 대한 알림을 모아서 처리한다.
 */
@Getter
public class NotificationBatch {
    private final BatchKey key;
    private final List<BatchActor> actors;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final Map<String, Object> extras;

    public NotificationBatch(
            BatchKey key,
            List<BatchActor> actors,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            Map<String, Object> extras
    ) {
        this.key = key;
        this.actors = new ArrayList<>(actors == null ? List.of() : actors);
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.extras = new HashMap<>(extras == null ? Map.of() : extras);
    }

    /**
     * 새 배치 생성.
     */
    public static NotificationBatch create(
            BatchKey key,
            BatchActor initialActor,
            int initialWindowSeconds,
            Map<String, Object> extras
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new NotificationBatch(
                key,
                List.of(initialActor),
                now,
                now.plusSeconds(initialWindowSeconds),
                extras != null ? extras : Map.of()
        );
    }

    /**
     * 배치에 새 액터 추가하고 만료 시간 연장 (최대 시간까지만).
     * 중복된 액터는 추가하지 않는다.
     *
     * @param maxWindowSeconds 배치 생성 시점부터의 최대 대기 시간
     */
    public NotificationBatch addActor(BatchActor actor, Map<String, Object> newExtras, int maxWindowSeconds) {
        List<BatchActor> updatedActors = new ArrayList<>(this.actors);

        // 중복 체크
        boolean exists = updatedActors.stream()
                .anyMatch(a -> a.id().equals(actor.id()));

        if (!exists) {
            updatedActors.add(actor);
        }

        // extras 병합 (새로운 extras로 기존 것을 덮어씀 - 최신 정보 유지)
        Map<String, Object> mergedExtras = new HashMap<>(this.extras);
        if (newExtras != null) {
            mergedExtras.putAll(newExtras);
        }

        // 만료 시간 연장
        LocalDateTime maxExpiresAt = this.createdAt.plusSeconds(maxWindowSeconds);

        // 기존 만료 시간보다 더 늦은 경우에만 연장
        LocalDateTime finalExpiresAt = maxExpiresAt.isAfter(this.expiresAt) ? maxExpiresAt : this.expiresAt;

        return new NotificationBatch(
                this.key,
                updatedActors,
                this.createdAt,
                finalExpiresAt,
                mergedExtras
        );
    }

    public boolean isExpired() {return LocalDateTime.now().isAfter(expiresAt);}

    public int getActorCount() {return actors.size();}

    // BatchKey 위임 편의 메서드
    public Long getRecipientId() {return key.recipientId();}

    public NotificationSubject getSubject() {return key.subject();}

    public NotificationAction getAction() {return key.action();}

    public Long getRelatedId() {return key.relatedId();}
}
