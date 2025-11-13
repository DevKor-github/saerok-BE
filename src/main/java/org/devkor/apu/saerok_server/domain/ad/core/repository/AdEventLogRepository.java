package org.devkor.apu.saerok_server.domain.ad.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.core.entity.AdEventLog;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdEventLogRepository {

    private final EntityManager em;

    public AdEventLog save(AdEventLog log) {
        em.persist(log);
        return log;
    }
}
