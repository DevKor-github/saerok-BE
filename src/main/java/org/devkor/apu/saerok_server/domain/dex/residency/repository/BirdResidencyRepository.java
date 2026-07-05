package org.devkor.apu.saerok_server.domain.dex.residency.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.residency.entity.BirdResidency;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BirdResidencyRepository {

    private final EntityManager em;

    public void save(BirdResidency residency) {
        em.persist(residency);
    }

    public void deleteByBirdId(Long birdId) {
        em.createQuery("DELETE FROM BirdResidency r WHERE r.bird.id = :birdId")
                .setParameter("birdId", birdId)
                .executeUpdate();
    }

    public List<BirdResidency> findByBirdIdWithTypes(Long birdId) {
        return em.createQuery("""
                        SELECT r
                        FROM BirdResidency r
                        JOIN FETCH r.residencyTypeEntity
                        JOIN FETCH r.rarityTypeEntity
                        WHERE r.bird.id = :birdId
                        ORDER BY r.id ASC
                        """, BirdResidency.class)
                .setParameter("birdId", birdId)
                .getResultList();
    }
}
