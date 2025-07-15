package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionCommentLike;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CollectionCommentLikeRepository {

    private final EntityManager em;

    public void save(UserBirdCollectionCommentLike commentLike) { 
        em.persist(commentLike); 
    }

    public void remove(UserBirdCollectionCommentLike commentLike) { 
        em.remove(commentLike); 
    }

    /**
     * 특정 사용자의 특정 댓글 좋아요 조회
     */
    public Optional<UserBirdCollectionCommentLike> findByUserIdAndCommentId(Long userId, Long commentId) {
        List<UserBirdCollectionCommentLike> results = em.createQuery(
                "SELECT cl FROM UserBirdCollectionCommentLike cl " +
                "WHERE cl.user.id = :userId AND cl.comment.id = :commentId", 
                UserBirdCollectionCommentLike.class)
                .setParameter("userId", userId)
                .setParameter("commentId", commentId)
                .setMaxResults(1)
                .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    /**
     * 특정 사용자의 특정 댓글 좋아요 존재 여부 확인
     */
    public boolean existsByUserIdAndCommentId(Long userId, Long commentId) {
        return !em.createQuery(
                "SELECT 1 FROM UserBirdCollectionCommentLike cl " +
                "WHERE cl.user.id = :userId AND cl.comment.id = :commentId",
                Integer.class)
                .setParameter("userId", userId)
                .setParameter("commentId", commentId)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    /**
     * 특정 댓글의 좋아요 수 조회
     */
    public long countByCommentId(Long commentId) {
        return em.createQuery(
                "SELECT COUNT(cl) FROM UserBirdCollectionCommentLike cl " +
                "WHERE cl.comment.id = :commentId", 
                Long.class)
                .setParameter("commentId", commentId)
                .getSingleResult();
    }

    /**
     * 여러 댓글의 좋아요 수를 한 번에 조회 (성능 최적화용)
     */
    public Map<Long, Long> countLikesByCommentIds(List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return Map.of();
        }

        // 초기값을 0으로 설정
        Map<Long, Long> likeCountMap = commentIds.stream()
                .collect(Collectors.toMap(Function.identity(), id -> 0L));

        List<Object[]> results = em.createQuery(
                "SELECT cl.comment.id, COUNT(cl) FROM UserBirdCollectionCommentLike cl " +
                "WHERE cl.comment.id IN :commentIds " +
                "GROUP BY cl.comment.id", 
                Object[].class)
                .setParameter("commentIds", commentIds)
                .getResultList();

        // 결과 반영
        for (Object[] result : results) {
            Long commentId = (Long) result[0];
            Long count = (Long) result[1];
            likeCountMap.put(commentId, count);
        }

        return likeCountMap;
    }

    /**
     * 여러 댓글에 대한 특정 사용자의 좋아요 상태를 한 번에 조회 (성능 최적화용)
     */
    public Map<Long, Boolean> findLikeStatusByUserIdAndCommentIds(Long userId, List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return Map.of();
        }

        List<Long> likedCommentIds = em.createQuery(
                "SELECT cl.comment.id FROM UserBirdCollectionCommentLike cl " +
                "WHERE cl.user.id = :userId AND cl.comment.id IN :commentIds", 
                Long.class)
                .setParameter("userId", userId)
                .setParameter("commentIds", commentIds)
                .getResultList();

        return commentIds.stream()
                .collect(Collectors.toMap(
                    commentId -> commentId, likedCommentIds::contains
                ));
    }
}
