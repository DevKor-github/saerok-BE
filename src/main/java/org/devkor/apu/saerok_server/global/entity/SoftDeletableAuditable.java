package org.devkor.apu.saerok_server.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class SoftDeletableAuditable extends Auditable {

    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    public void softDelete() {
        deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
