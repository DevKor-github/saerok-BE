package org.devkor.apu.saerok_server.domain.ad.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Slot;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SlotRepository {

    private final EntityManager em;

    public Slot save(Slot slot) {
        em.persist(slot);
        return slot;
    }

    public Optional<Slot> findById(Long id) {
        Slot slot = em.find(Slot.class, id);
        return Optional.ofNullable(slot);
    }

    public Optional<Slot> findByName(String name) {
        return em.createQuery(
                        "SELECT s FROM Slot s WHERE s.name = :name",
                        Slot.class
                )
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    public List<Slot> findAll() {
        return em.createQuery(
                "SELECT s FROM Slot s ORDER BY s.id ASC",
                Slot.class
        ).getResultList();
    }

    public void delete(Slot slot) {
        em.remove(slot);
    }
}
