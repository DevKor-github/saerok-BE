package org.devkor.apu.saerok_server.domain.dex.bookmark.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookmarkRepository {

    private final EntityManager em;

    /**
     * 사용자의 모든 북마크를 조회합니다.
     */
    public List<UserBirdBookmark> findAllByUserId(Long userId) {
        return em.createQuery(
                "SELECT b FROM UserBirdBookmark b " +
                "JOIN FETCH b.bird bird " +
                "WHERE b.user.id = :userId " +
                "AND bird.deletedAt IS NULL " +
                "ORDER BY b.createdAt DESC", UserBirdBookmark.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * 사용자와 조류 ID를 기준으로 북마크를 조회합니다.
     */
    public Optional<UserBirdBookmark> findByUserIdAndBirdId(Long userId, Long birdId) {
        List<UserBirdBookmark> results = em.createQuery(
                "SELECT b FROM UserBirdBookmark b " +
                "JOIN b.bird bird " +
                "WHERE b.user.id = :userId " +
                "AND b.bird.id = :birdId " +
                "AND bird.deletedAt IS NULL", UserBirdBookmark.class)
                .setParameter("userId", userId)
                .setParameter("birdId", birdId)
                .setMaxResults(1)
                .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    /**
     * 사용자와 조류 ID를 기준으로 북마크 존재 여부를 확인합니다.
     */
    public boolean existsByUserIdAndBirdId(Long userId, Long birdId) {
        return !em.createQuery(
                "SELECT 1 FROM UserBirdBookmark b " +
                "JOIN b.bird bird " +
                "WHERE b.user.id = :userId " +
                "AND b.bird.id = :birdId " +
                "AND bird.deletedAt IS NULL", Integer.class)
                .setParameter("userId", userId)
                .setParameter("birdId", birdId)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    /**
     * 사용자의 북마크와 조류 관련 정보를 함께 조회합니다.
     */
    public List<UserBirdBookmark> findAllWithBirdDetailsByUserId(Long userId) {
        return em.createQuery(
                "SELECT b FROM UserBirdBookmark b " +
                "JOIN FETCH b.bird bird " +
                "WHERE b.user.id = :userId " +
                "AND bird.deletedAt IS NULL " +
                "ORDER BY b.createdAt DESC", UserBirdBookmark.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public void save(UserBirdBookmark bookmark) {
        em.persist(bookmark);
    }

    public void remove(UserBirdBookmark bookmark) {
        em.remove(bookmark);
    }

    public void deleteByUserId(Long userId) {
        em.createQuery(
                        "DELETE FROM UserBirdBookmark b WHERE b.user.id = :userId"
                )
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
