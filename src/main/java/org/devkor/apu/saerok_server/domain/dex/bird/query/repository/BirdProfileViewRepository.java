package org.devkor.apu.saerok_server.domain.dex.bird.query.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
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

    public List<String> findKoreanNamesByKeyword(String keyword) {
        List<String> prefixMatches = em.createQuery(
                "SELECT b.name.koreanName FROM BirdProfileView b " +
                "WHERE b.name.koreanName LIKE :prefix AND b.deletedAt IS NULL " +
                "ORDER BY b.name.koreanName ASC", String.class)
                .setParameter("prefix", keyword + "%")
                .setMaxResults(10)
                .getResultList();

        if (prefixMatches.size() < 10) {
            List<String> containsMatches = em.createQuery(
                    "SELECT b.name.koreanName FROM BirdProfileView b " +
                    "WHERE b.name.koreanName LIKE :contains AND b.name.koreanName NOT LIKE :prefix AND b.deletedAt IS NULL " +
                    "ORDER BY b.name.koreanName ASC", String.class)
                    .setParameter("contains", "%" + keyword + "%")
                    .setParameter("prefix", keyword + "%")
                    .setMaxResults(10 - prefixMatches.size())
                    .getResultList();
            List<String> result = new ArrayList<>(prefixMatches);
            result.addAll(containsMatches);
            return result;
        }

        return prefixMatches;
    }
    
    @Transactional
    public void refreshMaterializedView() {
        em.createNativeQuery("REFRESH MATERIALIZED VIEW bird_profile_mv")
                .executeUpdate();
    }
}