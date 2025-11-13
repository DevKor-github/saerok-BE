package org.devkor.apu.saerok_server.domain.ad.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Ad;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdRepository {

    private final EntityManager em;

    public Ad save(Ad ad) {
        em.persist(ad);
        return ad;
    }

    public Optional<Ad> findById(Long id) {
        Ad ad = em.find(Ad.class, id);
        return Optional.ofNullable(ad);
    }

    public List<Ad> findAll() {
        return em.createQuery(
                "SELECT a FROM Ad a ORDER BY a.id DESC",
                Ad.class
        ).getResultList();
    }

    public void delete(Ad ad) {
        em.remove(ad);
    }
}
