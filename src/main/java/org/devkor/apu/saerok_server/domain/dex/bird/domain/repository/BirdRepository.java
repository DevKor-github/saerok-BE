package org.devkor.apu.saerok_server.domain.dex.bird.domain.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.entity.Bird;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BirdRepository {

    private final EntityManager em;

    /**
     * Bird 엔티티를 조회합니다. soft delete된 항목은 조회하지 않습니다.
     * @param id
     * @return
     */
    public Optional<Bird> findById(Long id) {
        return em.createQuery("SELECT b FROM Bird b WHERE b.id = :id AND b.deletedAt IS NULL", Bird.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }
}
