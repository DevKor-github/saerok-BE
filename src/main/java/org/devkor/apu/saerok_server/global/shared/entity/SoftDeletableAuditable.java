package org.devkor.apu.saerok_server.global.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class SoftDeletableAuditable extends Auditable {

    @Column(name = "deleted_at")
    protected OffsetDateTime deletedAt;

    public void softDelete() {
        updatedAt = OffsetDateTime.now();
        deletedAt = OffsetDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
