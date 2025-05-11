package org.devkor.apu.saerok_server.domain.dex.bookmark.core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
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
                "WHERE b.user.id = :userId " +
                "ORDER BY b.createdAt DESC", UserBirdBookmark.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * 사용자와 조류 ID를 기준으로 북마크 존재 여부를 확인합니다.
     * @param userId 사용자 ID
     * @param birdId 조류 ID
     * @return 북마크 존재 여부
     */
    public boolean existsByUserIdAndBirdId(Long userId, Long birdId) {
        Long count = em.createQuery(
                "SELECT COUNT(b) FROM UserBirdBookmark b " +
                "WHERE b.user.id = :userId " +
                "AND b.bird.id = :birdId", Long.class)
                .setParameter("userId", userId)
                .setParameter("birdId", birdId)
                .getSingleResult();
        return count > 0;
    }

    /**
     * 사용자의 북마크와 조류 관련 정보를 함께 조회합니다.
     */
    public List<UserBirdBookmark> findAllWithBirdDetailsByUserId(Long userId) {
        return em.createQuery(
                "SELECT b FROM UserBirdBookmark b " +
                "JOIN FETCH b.bird " +
                "WHERE b.user.id = :userId " +
                "ORDER BY b.createdAt DESC", UserBirdBookmark.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public void save(UserBirdBookmark bookmark) {
        em.persist(bookmark);
    }

    public void delete(UserBirdBookmark bookmark) {
        em.remove(bookmark);
    }
}
