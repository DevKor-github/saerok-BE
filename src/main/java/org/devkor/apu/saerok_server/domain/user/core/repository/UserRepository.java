package org.devkor.apu.saerok_server.domain.user.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.SignupStatusType;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;

    public Optional<User> findById(Long id) {
        return em.createQuery("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL ", User.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public Optional<User> findDeletedUserById(Long id) {
        return em.createQuery("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NOT NULL", User.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public User save(User user) {
        em.persist(user);
        return user;
    }

    public Optional<User> findByNickname(String nickname) {
        return em.createQuery("SELECT u FROM User u WHERE u.nickname = :nickname", User.class)
                .setParameter("nickname", nickname)
                .getResultStream()
                .findFirst();
    }

    public List<Long> findActiveUserIds() {
        return em.createQuery(
                        "SELECT u.id FROM User u " +
                        "WHERE u.deletedAt IS NULL AND u.signupStatus <> :withdrawn",
                        Long.class)
                .setParameter("withdrawn", SignupStatusType.WITHDRAWN)
                .getResultList();
    }

    public List<Long> findActiveUserIds(int offset, int limit) {
        return em.createQuery(
                        "SELECT u.id FROM User u " +
                        "WHERE u.deletedAt IS NULL AND u.signupStatus <> :withdrawn " +
                        "ORDER BY u.id",
                        Long.class)
                .setParameter("withdrawn", SignupStatusType.WITHDRAWN)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<User> findActiveNicknameUsers(String nicknameQuery, int offset, int limit) {
        String jpql = """
                SELECT u FROM User u
                WHERE u.deletedAt IS NULL
                  AND u.signupStatus <> :withdrawn
                  AND u.nickname IS NOT NULL
                  AND TRIM(u.nickname) <> ''
                """;
        if (nicknameQuery != null) {
            jpql += " AND u.nickname LIKE :nicknameQuery";
        }
        jpql += " ORDER BY u.nickname ASC, u.id ASC";

        var query = em.createQuery(jpql, User.class)
                .setParameter("withdrawn", SignupStatusType.WITHDRAWN)
                .setFirstResult(offset)
                .setMaxResults(limit);

        if (nicknameQuery != null) {
            query.setParameter("nicknameQuery", "%" + nicknameQuery + "%");
        }

        return query.getResultList();
    }

    public long countActiveNicknameUsers(String nicknameQuery) {
        String jpql = """
                SELECT COUNT(u) FROM User u
                WHERE u.deletedAt IS NULL
                  AND u.signupStatus <> :withdrawn
                  AND u.nickname IS NOT NULL
                  AND TRIM(u.nickname) <> ''
                """;
        if (nicknameQuery != null) {
            jpql += " AND u.nickname LIKE :nicknameQuery";
        }

        var query = em.createQuery(jpql, Long.class)
                .setParameter("withdrawn", SignupStatusType.WITHDRAWN);

        if (nicknameQuery != null) {
            query.setParameter("nicknameQuery", "%" + nicknameQuery + "%");
        }

        return query.getSingleResult();
    }

    public List<User> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return em.createQuery(
                        "SELECT u FROM User u WHERE u.id IN :ids",
                        User.class)
                .setParameter("ids", ids)
                .getResultList();
    }
}
