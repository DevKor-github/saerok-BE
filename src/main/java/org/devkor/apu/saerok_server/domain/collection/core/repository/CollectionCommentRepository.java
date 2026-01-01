package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CollectionCommentRepository {

    private final EntityManager em;

    public void save(UserBirdCollectionComment comment) { em.persist(comment); }

    public Optional<UserBirdCollectionComment> findById(Long id) {
        return Optional.ofNullable(em.find(UserBirdCollectionComment.class, id));
    }

    public void remove(UserBirdCollectionComment comment) { em.remove(comment); }

    public List<UserBirdCollectionComment> findByCollectionId(Long collectionId) {
        return em.createQuery(
                        "SELECT c FROM UserBirdCollectionComment c " +
                                "WHERE c.collection.id = :collectionId " +
                                "ORDER BY c.createdAt ASC",
                        UserBirdCollectionComment.class)
                .setParameter("collectionId", collectionId)
                .getResultList();
    }

    public long countByCollectionId(Long collectionId) {
        return em.createQuery(
                        "SELECT COUNT(c) FROM UserBirdCollectionComment c " +
                                "WHERE c.collection.id = :collectionId " +
                                "AND c.status = org.devkor.apu.saerok_server.domain.collection.core.entity.CommentStatus.ACTIVE",
                        Long.class)
                .setParameter("collectionId", collectionId)
                .getSingleResult();
    }

    public boolean hasReplies(Long commentId) {
        Long count = em.createQuery(
                        "SELECT COUNT(c) FROM UserBirdCollectionComment c WHERE c.parent.id = :commentId",
                        Long.class)
                .setParameter("commentId", commentId)
                .getSingleResult();
        return count > 0;
    }

    /* ────────────────────────────── 성능 최적화: 배치 메서드 ────────────────────────────── */

    /**
     * 여러 컬렉션의 ACTIVE 댓글 수를 한 번에 조회
     * 반환 맵은 요청한 ID를 모두 포함하며, 없으면 0으로 채운다.
     */
    public Map<Long, Long> countByCollectionIds(List<Long> collectionIds) {
        if (collectionIds == null || collectionIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = em.createQuery(
                        "SELECT c.collection.id, COUNT(c) " +
                                "FROM UserBirdCollectionComment c " +
                                "WHERE c.collection.id IN :ids " +
                                "AND c.status = org.devkor.apu.saerok_server.domain.collection.core.entity.CommentStatus.ACTIVE " +
                                "GROUP BY c.collection.id",
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
}
