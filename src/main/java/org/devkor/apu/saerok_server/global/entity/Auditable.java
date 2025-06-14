package org.devkor.apu.saerok_server.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class Auditable {

    @Column(name = "created_at", updatable = false)
    protected OffsetDateTime createdAt;

    @Column(name = "updated_at")
    protected OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
        postOnCreate();
    }

    // 각 엔티티별로 INSERT 시점에 추가 로직을 실행하고 싶을 때 override해서 쓸 수 있는 hook
    protected void postOnCreate() {}

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
