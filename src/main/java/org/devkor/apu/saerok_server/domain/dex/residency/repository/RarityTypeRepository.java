package org.devkor.apu.saerok_server.domain.dex.residency.repository;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.residency.entity.RarityType;
import org.devkor.apu.saerok_server.domain.dex.residency.entity.RarityTypeEntity;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RarityTypeRepository {

    private final EntityManager em;

    public Optional<RarityTypeEntity> findByCode(RarityType code) {
        return em.createQuery(
                        "SELECT r FROM RarityTypeEntity r WHERE r.code = :code",
                        RarityTypeEntity.class)
                .setParameter("code", code)
                .getResultStream()
                .findFirst();
    }
}
