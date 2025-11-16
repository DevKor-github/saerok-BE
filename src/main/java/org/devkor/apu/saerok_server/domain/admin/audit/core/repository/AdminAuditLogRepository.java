package org.devkor.apu.saerok_server.domain.admin.audit.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditLog;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminAuditLogRepository {

    private final EntityManager em;

    public void save(AdminAuditLog log) {
        em.persist(log);
    }

    public List<AdminAuditLog> findAllOrderByCreatedAtDesc() {
        return em.createQuery(
                "SELECT l FROM AdminAuditLog l " +
                        "JOIN FETCH l.admin a " +
                        "ORDER BY l.createdAt DESC", AdminAuditLog.class
        ).getResultList();
    }

    public List<AdminAuditLog> findPageOrderByCreatedAtDesc(int page, int size) {
        int offset = (page - 1) * size;
        return em.createQuery(
                        "SELECT l FROM AdminAuditLog l " +
                                "JOIN FETCH l.admin a " +
                                "ORDER BY l.createdAt DESC", AdminAuditLog.class
                )
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }
}
