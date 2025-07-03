package org.devkor.apu.saerok_server.domain.dex.bookmark.core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookmarkRepository {

    private final EntityManager em;
    private final UserRepository userRepository;
    private final BirdRepository birdRepository;

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
     * 사용자와 조류 ID를 기준으로 북마크 존재 여부를 확인합니다.
     * @param userId 사용자 ID
     * @param birdId 조류 ID
     * @return 북마크 존재 여부
     */
    public boolean existsByUserIdAndBirdId(Long userId, Long birdId) {
        Long count = em.createQuery(
                "SELECT COUNT(b) FROM UserBirdBookmark b " +
                "JOIN b.bird bird " +
                "WHERE b.user.id = :userId " +
                "AND b.bird.id = :birdId " +
                "AND bird.deletedAt IS NULL", Long.class)
                .setParameter("userId", userId)
                .setParameter("birdId", birdId)
                .setMaxResults(1)
                .getSingleResult();
        return count > 0;
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
    
    /**
     * 사용자와 조류 ID를 기준으로 북마크를 삭제합니다.
     * @param userId 사용자 ID
     * @param birdId 조류 ID
     */
    public void deleteByUserIdAndBirdId(Long userId, Long birdId) {
        em.createQuery(
                "DELETE FROM UserBirdBookmark b " +
                "WHERE b.user.id = :userId " +
                "AND b.bird.id = :birdId")
                .setParameter("userId", userId)
                .setParameter("birdId", birdId)
                .executeUpdate();
    }

    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<Bird> findBirdById(Long birdId) {
        return birdRepository.findById(birdId);
    }
}
