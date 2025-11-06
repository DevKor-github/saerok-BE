package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionLike;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CollectionLikeRepository {

    private final EntityManager em;

    public void save(UserBirdCollectionLike like) { em.persist(like); }

    public void remove(UserBirdCollectionLike like) { em.remove(like); }

    /**
     * 특정 사용자의 특정 컬렉션 좋아요 조회
     */
    public Optional<UserBirdCollectionLike> findByUserIdAndCollectionId(Long userId, Long collectionId) {
        List<UserBirdCollectionLike> results = em.createQuery(
                        "SELECT l FROM UserBirdCollectionLike l " +
                                "WHERE l.user.id = :userId AND l.collection.id = :collectionId",
                        UserBirdCollectionLike.class)
                .setParameter("userId", userId)
                .setParameter("collectionId", collectionId)
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    /**
     * 특정 사용자의 특정 컬렉션 좋아요 존재 여부 확인 (단건 조회)
     */
    public boolean existsByUserIdAndCollectionId(Long userId, Long collectionId) {
        return !em.createQuery(
                        "SELECT 1 FROM UserBirdCollectionLike l " +
                                "WHERE l.user.id = :userId AND l.collection.id = :collectionId",
                        Integer.class)
                .setParameter("userId", userId)
                .setParameter("collectionId", collectionId)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    /**
     * 특정 컬렉션을 좋아요한 사용자 목록 조회 (유저 목록 조회)
     */
    public List<User> findLikersByCollectionId(Long collectionId) {
        return em.createQuery(
                        "SELECT l.user FROM UserBirdCollectionLike l " +
                                "WHERE l.collection.id = :collectionId " +
                                "ORDER BY l.createdAt DESC",
                        User.class)
                .setParameter("collectionId", collectionId)
                .getResultList();
    }

    /**
     * 사용자가 좋아요한 컬렉션 목록 조회 (컬렉션 목록 조회)
     */
    public List<UserBirdCollection> findLikedCollectionsByUserId(Long userId) {
        return em.createQuery(
                        "SELECT l.collection FROM UserBirdCollectionLike l " +
                                "WHERE l.user.id = :userId " +
                                "ORDER BY l.createdAt DESC",
                        UserBirdCollection.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * 특정 컬렉션의 좋아요 수 조회
     */
    public long countByCollectionId(Long collectionId) {
        return em.createQuery(
                        "SELECT COUNT(l) FROM UserBirdCollectionLike l " +
                                "WHERE l.collection.id = :collectionId",
                        Long.class)
                .setParameter("collectionId", collectionId)
                .getSingleResult();
    }

    /* ────────────────────────────── 성능 최적화: 배치 메서드 ────────────────────────────── */

    /**
     * 여러 컬렉션의 좋아요 수를 한 번에 조회
     * 반환 맵은 요청한 ID를 모두 포함하며, 없으면 0으로 채운다.
     */
    public Map<Long, Long> countLikesByCollectionIds(List<Long> collectionIds) {
        if (collectionIds == null || collectionIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = em.createQuery(
                        "SELECT l.collection.id, COUNT(l) " +
                                "FROM UserBirdCollectionLike l " +
                                "WHERE l.collection.id IN :ids " +
                                "GROUP BY l.collection.id",
                        Object[].class)
                .setParameter("ids", collectionIds)
                .getResultList();

        Map<Long, Long> result = new LinkedHashMap<>();
        for (Long id : collectionIds) result.put(id, 0L);
        for (Object[] row : rows) {
            Long id = (Long) row[0];
            Long cnt = (Long) row[1];
            result.put(id, cnt);
        }
        return result;
    }

    /**
     * 주어진 사용자 기준으로 여러 컬렉션에 대한 좋아요 여부를 한 번에 조회
     * 반환 맵은 요청한 ID를 모두 포함하며, 없으면 false로 채운다.
     */
    public Map<Long, Boolean> findLikeStatusByUserIdAndCollectionIds(Long userId, List<Long> collectionIds) {
        if (userId == null || collectionIds == null || collectionIds.isEmpty()) {
            return Map.of();
        }

        List<Long> likedIds = em.createQuery(
                        "SELECT l.collection.id " +
                                "FROM UserBirdCollectionLike l " +
                                "WHERE l.user.id = :userId AND l.collection.id IN :ids",
                        Long.class)
                .setParameter("userId", userId)
                .setParameter("ids", collectionIds)
                .getResultList();

        Map<Long, Boolean> result = new LinkedHashMap<>();
        for (Long id : collectionIds) result.put(id, false);
        for (Long id : likedIds) result.put(id, true);
        return result;
    }
}
