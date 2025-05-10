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
     * 사용자 ID와 조류 ID로 단일 북마크를 조회합니다.
     * 북마크가 true인지 아닌지 여부와 단일 북마크 정보가 필요한 상황에 이용 가능. (후자의 상황이 없다면 추후 성능을 위해 수정 예정)
     */
    public Optional<UserBirdBookmark> findByUserIdAndBirdId(Long userId, Long birdId) {
        try {
            UserBirdBookmark bookmark = em.createQuery(
                    "SELECT b FROM UserBirdBookmark b " +
                    "WHERE b.user.id = :userId " +
                    "AND b.bird.id = :birdId", UserBirdBookmark.class)
                    .setParameter("userId", userId)
                    .setParameter("birdId", birdId)
                    .getSingleResult();
            return Optional.of(bookmark);
        } catch (NoResultException e) {
            return Optional.empty();
        }
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
