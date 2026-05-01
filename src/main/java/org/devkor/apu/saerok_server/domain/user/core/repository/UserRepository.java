package org.devkor.apu.saerok_server.domain.user.core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
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

    public List<User> searchActiveUsers(String nicknameQuery, Long idQuery, int offset, int limit) {
        String jpql = """
                SELECT u FROM User u
                WHERE u.deletedAt IS NULL
                  AND u.signupStatus <> :withdrawn
                  AND u.nickname IS NOT NULL
                  AND TRIM(u.nickname) <> ''
                """;
        jpql += buildSearchPredicate(nicknameQuery, idQuery);
        jpql += " ORDER BY u.nickname ASC, u.id ASC";

        var query = em.createQuery(jpql, User.class)
                .setParameter("withdrawn", SignupStatusType.WITHDRAWN)
                .setFirstResult(offset)
                .setMaxResults(limit);

        bindSearchParameters(query, nicknameQuery, idQuery);

        return query.getResultList();
    }

    public long countSearchActiveUsers(String nicknameQuery, Long idQuery) {
        String jpql = """
                SELECT COUNT(u) FROM User u
                WHERE u.deletedAt IS NULL
                  AND u.signupStatus <> :withdrawn
                  AND u.nickname IS NOT NULL
                  AND TRIM(u.nickname) <> ''
                """;
        jpql += buildSearchPredicate(nicknameQuery, idQuery);

        var query = em.createQuery(jpql, Long.class)
                .setParameter("withdrawn", SignupStatusType.WITHDRAWN);

        bindSearchParameters(query, nicknameQuery, idQuery);

        return query.getSingleResult();
    }

    private String buildSearchPredicate(String nicknameQuery, Long idQuery) {
        boolean hasNickname = nicknameQuery != null;
        boolean hasId = idQuery != null;
        if (hasNickname && hasId) {
            return " AND (u.nickname LIKE :nicknameQuery OR u.id = :idQuery)";
        }
        if (hasNickname) {
            return " AND u.nickname LIKE :nicknameQuery";
        }
        if (hasId) {
            return " AND u.id = :idQuery";
        }
        return "";
    }

    private void bindSearchParameters(Query query, String nicknameQuery, Long idQuery) {
        if (nicknameQuery != null) {
            query.setParameter("nicknameQuery", "%" + nicknameQuery + "%");
        }
        if (idQuery != null) {
            query.setParameter("idQuery", idQuery);
        }
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
