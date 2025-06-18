package org.devkor.apu.saerok_server.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class CreatedAtOnly {

    @Column(name = "created_at", updatable = false)
    protected OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
