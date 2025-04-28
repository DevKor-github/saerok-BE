package org.devkor.apu.saerok_server.domain.dex.bird.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.entity.BirdProfile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BirdProfileRepository {

    private final EntityManager em;

    public Optional<BirdProfile> findById(Long id) {
        BirdProfile birdProfile = em.find(BirdProfile.class, id);
        return Optional.ofNullable(birdProfile);
    }

    public List<BirdProfile> findAll() {
        return em.createQuery("SELECT b FROM BirdProfile b", BirdProfile.class).getResultList();
    }
}