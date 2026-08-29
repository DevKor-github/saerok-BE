package org.devkor.apu.saerok_server.domain.dex.bird.core.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdImage;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BirdImageRepository {

    private final EntityManager em;

    public void save(BirdImage image) {
        em.persist(image);
    }

    public void deleteByBirdId(Long birdId) {
        em.createQuery("DELETE FROM BirdImage i WHERE i.bird.id = :birdId")
                .setParameter("birdId", birdId)
                .executeUpdate();
    }

    public List<String> findObjectKeysByBirdId(Long birdId) {
        return em.createQuery("SELECT i.objectKey FROM BirdImage i WHERE i.bird.id = :birdId", String.class)
                .setParameter("birdId", birdId)
                .getResultList();
    }
}
