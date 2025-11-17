package org.devkor.apu.saerok_server.domain.admin.stat.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;

import java.time.OffsetDateTime;

@Entity
@Getter
@NoArgsConstructor
public class BirdIdRequestHistory extends CreatedAtOnly {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "collection_id", nullable = true) // 원본 새록이 삭제된, 닫힌 히스토리의 경우 컬렉션 id를 null로 해서 보존
    private UserBirdCollection collection;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "resolution_seconds")
    private Long resolutionSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_kind", length = 16)
    private ResolutionKind resolutionKind; // ADOPT만 집계 대상

    private BirdIdRequestHistory(UserBirdCollection collection, OffsetDateTime startedAt) {
        this.collection = collection;
        this.startedAt = startedAt;
    }

    public static BirdIdRequestHistory start(UserBirdCollection collection, OffsetDateTime startedAt) {
        return new BirdIdRequestHistory(collection, startedAt);
    }

    public void resolve(OffsetDateTime resolvedAt, ResolutionKind kind) {
        if (this.resolvedAt != null) return; // idempotent
        this.resolvedAt = resolvedAt;
        this.resolutionKind = kind;
        this.resolutionSeconds = Math.max(0, resolvedAt.toEpochSecond() - startedAt.toEpochSecond());
    }

    public boolean isOpen() {
        return resolvedAt == null;
    }
}
