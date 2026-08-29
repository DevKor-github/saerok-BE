package org.devkor.apu.saerok_server.domain.dex.residency.repository;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.residency.entity.ResidencyType;
import org.devkor.apu.saerok_server.domain.dex.residency.entity.ResidencyTypeEntity;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ResidencyTypeRepository {

    private final EntityManager em;

    public Optional<ResidencyTypeEntity> findByCode(ResidencyType code) {
        return em.createQuery(
                        "SELECT r FROM ResidencyTypeEntity r WHERE r.code = :code",
                        ResidencyTypeEntity.class)
                .setParameter("code", code)
                .getResultStream()
                .findFirst();
    }
}
