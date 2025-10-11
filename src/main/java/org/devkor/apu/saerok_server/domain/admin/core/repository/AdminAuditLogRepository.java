package org.devkor.apu.saerok_server.domain.admin.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.core.entity.AdminAuditLog;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminAuditLogRepository {

    private final EntityManager em;

    public void save(AdminAuditLog log) {
        em.persist(log);
    }
}
