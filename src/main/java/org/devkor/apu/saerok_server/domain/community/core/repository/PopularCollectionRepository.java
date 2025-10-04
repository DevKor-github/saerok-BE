package org.devkor.apu.saerok_server.domain.community.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.community.core.entity.PopularCollection;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PopularCollectionRepository {

    private final EntityManager em;

    public void save(PopularCollection popularCollection) {
        em.persist(popularCollection);
    }

    public boolean existsByCollectionId(Long collectionId) {
        return !em.createQuery(
                "SELECT 1 FROM PopularCollection pc " +
                "WHERE pc.collection.id = :collectionId",
                Integer.class)
                .setParameter("collectionId", collectionId)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    public Map<Long, Boolean> existsByCollectionIds(List<Long> collectionIds) {
        if (collectionIds.isEmpty()) {
            return Map.of();
        }

        List<Long> popularCollectionIds = em.createQuery(
                "SELECT pc.collection.id FROM PopularCollection pc " +
                "WHERE pc.collection.id IN :collectionIds",
                Long.class)
                .setParameter("collectionIds", collectionIds)
                .getResultList();

        return collectionIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        popularCollectionIds::contains
                ));
    }
}
