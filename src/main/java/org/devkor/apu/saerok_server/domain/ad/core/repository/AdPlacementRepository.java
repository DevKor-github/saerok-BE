package org.devkor.apu.saerok_server.domain.ad.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.core.entity.AdPlacement;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdPlacementRepository {

    private final EntityManager em;

    public AdPlacement save(AdPlacement placement) {
        em.persist(placement);
        return placement;
    }

    public Optional<AdPlacement> findById(Long id) {
        AdPlacement placement = em.find(AdPlacement.class, id);
        return Optional.ofNullable(placement);
    }

    public List<AdPlacement> findAll() {
        return em.createQuery(
                "SELECT ap FROM AdPlacement ap " +
                        "JOIN FETCH ap.ad a " +
                        "JOIN FETCH ap.slot s " +
                        "ORDER BY ap.id DESC",
                AdPlacement.class
        ).getResultList();
    }

    public void delete(AdPlacement placement) {
        em.remove(placement);
    }

    public List<AdPlacement> findActivePlacements(Long slotId, LocalDate today) {
        return em.createQuery(
                        "SELECT ap FROM AdPlacement ap " +
                                "JOIN FETCH ap.ad a " +
                                "WHERE ap.slot.id = :slotId " +
                                "AND ap.enabled = true " +
                                "AND ap.startDate <= :today " +
                                "AND ap.endDate >= :today",
                        AdPlacement.class
                )
                .setParameter("slotId", slotId)
                .setParameter("today", today)
                .getResultList();
    }
}
