package org.devkor.apu.saerok_server.domain.dex.bird.query.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BirdProfileViewRepository {

    private final EntityManager em;

    public Optional<BirdProfileView> findById(Long id) {
        BirdProfileView birdProfileView = em.find(BirdProfileView.class, id);
        return Optional.ofNullable(birdProfileView);
    }

    public List<BirdProfileView> findAll() {
        return em.createQuery("SELECT b FROM BirdProfileView b", BirdProfileView.class).getResultList();
    }
}