package org.devkor.apu.saerok_server.domain.dex.bird.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdHabitat;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BirdHabitatRepository {

    private final EntityManager em;

    public void save(BirdHabitat habitat) {
        em.persist(habitat);
    }

    public void deleteByBirdId(Long birdId) {
        em.createQuery("DELETE FROM BirdHabitat h WHERE h.bird.id = :birdId")
                .setParameter("birdId", birdId)
                .executeUpdate();
    }
}
