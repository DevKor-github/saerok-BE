package org.devkor.apu.saerok_server.domain.community.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.community.core.repository.dto.TrendingCollectionCandidate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class TrendingCollectionRepository {

    private final EntityManager em;

    public List<TrendingCollectionCandidate> findRecentPublicCollections(OffsetDateTime createdAfter) {
        return em.createQuery(
                        "SELECT new org.devkor.apu.saerok_server.domain.community.core.repository.dto.TrendingCollectionCandidate(" +
                                " c.id, c.createdAt) " +
                                "FROM UserBirdCollection c " +
                                "WHERE c.accessLevel = :public AND c.createdAt >= :createdAfter",
                        TrendingCollectionCandidate.class)
                .setParameter("public", AccessLevelType.PUBLIC)
                .setParameter("createdAfter", createdAfter)
                .getResultList();
    }

    public Map<Long, List<OffsetDateTime>> findLikeCreatedAtByCollectionIds(List<Long> collectionIds) {
        if (collectionIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = em.createQuery(
                        "SELECT l.collection.id, l.createdAt " +
                                "FROM UserBirdCollectionLike l " +
                                "WHERE l.collection.id IN :ids",
                        Object[].class)
                .setParameter("ids", collectionIds)
                .getResultList();

        Map<Long, List<OffsetDateTime>> result = new LinkedHashMap<>();
        for (Long id : collectionIds) {
            result.put(id, new ArrayList<>());
        }
        for (Object[] row : rows) {
            Long collectionId = (Long) row[0];
            OffsetDateTime createdAt = (OffsetDateTime) row[1];
            result.computeIfAbsent(collectionId, k -> new ArrayList<>()).add(createdAt);
        }
        return result;
    }

    public Map<Long, Map<Long, OffsetDateTime>> findLastCommentAtByCollectionIds(List<Long> collectionIds) {
        if (collectionIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = em.createQuery(
                        "SELECT c.collection.id, c.user.id, MAX(c.createdAt) " +
                                "FROM UserBirdCollectionComment c " +
                                "WHERE c.collection.id IN :ids " +
                                "GROUP BY c.collection.id, c.user.id",
                        Object[].class)
                .setParameter("ids", collectionIds)
                .getResultList();

        Map<Long, Map<Long, OffsetDateTime>> result = new LinkedHashMap<>();
        for (Long id : collectionIds) {
            result.put(id, new LinkedHashMap<>());
        }
        for (Object[] row : rows) {
            Long collectionId = (Long) row[0];
            Long userId = (Long) row[1];
            OffsetDateTime createdAt = (OffsetDateTime) row[2];
            result.computeIfAbsent(collectionId, k -> new LinkedHashMap<>())
                    .put(userId, createdAt);
        }
        return result;
    }
}
