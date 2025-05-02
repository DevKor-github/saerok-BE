package org.devkor.apu.saerok_server.domain.dex.bird.query.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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

    public List<BirdProfileView> findByCreatedAtAfter(OffsetDateTime since) {
        return em.createQuery("SELECT b FROM BirdProfileView b WHERE b.createdAt > :since " +
                        "AND b.deletedAt IS NULL", BirdProfileView.class)
                .setParameter("since", since)
                .getResultList();
    }

    public List<BirdProfileView> findByUpdatedAtAfter(OffsetDateTime since) {
        return em.createQuery("SELECT b FROM BirdProfileView b WHERE b.updatedAt > :since " +
                        "AND b.createdAt <= :since " +
                        "AND b.deletedAt IS NULL", BirdProfileView.class)
                .setParameter("since", since)
                .getResultList();
    }

    public List<BirdProfileView> findByDeletedAtAfter(OffsetDateTime since) {
        return em.createQuery("SELECT b FROM BirdProfileView b WHERE b.createdAt <= :since " +
                        "AND b.deletedAt > :since", BirdProfileView.class)
                .setParameter("since", since)
                .getResultList();
    }

    @Transactional
    public void refreshMaterializedView() {
        em.createNativeQuery("REFRESH MATERIALIZED VIEW bird_profile_mv")
                .executeUpdate();
    }
}