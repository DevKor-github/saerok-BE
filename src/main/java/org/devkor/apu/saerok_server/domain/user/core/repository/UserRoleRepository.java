package org.devkor.apu.saerok_server.domain.user.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRole;
import org.devkor.apu.saerok_server.global.security.permission.Role;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRoleRepository {

    private final EntityManager em;

    public void save(UserRole userRole) {
        em.persist(userRole);
    }

    public List<UserRole> findByUser(User user) {
        return em.createQuery(
                        "SELECT ur FROM UserRole ur " +
                                "JOIN FETCH ur.role r " +
                                "WHERE ur.user = :user " +
                                "ORDER BY r.code",
                        UserRole.class
                )
                .setParameter("user", user)
                .getResultList();
    }

    public int deleteByUserId(Long userId) {
        return em.createQuery("DELETE FROM UserRole ur WHERE ur.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public boolean existsByRole(Role role) {
        Long count = em.createQuery(
                        "SELECT COUNT(ur) FROM UserRole ur WHERE ur.role = :role",
                        Long.class
                )
                .setParameter("role", role)
                .getSingleResult();
        return count > 0;
    }

    public boolean existsByUserIdAndRoleCode(Long userId, String roleCode) {
        Long count = em.createQuery(
                        "SELECT COUNT(ur) FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.code = :roleCode",
                        Long.class
                )
                .setParameter("userId", userId)
                .setParameter("roleCode", roleCode)
                .getSingleResult();
        return count > 0;
    }

    public Optional<UserRole> findByUserIdAndRoleCode(Long userId, String roleCode) {
        return em.createQuery(
                        "SELECT ur FROM UserRole ur " +
                                "JOIN FETCH ur.role r " +
                                "JOIN FETCH ur.user u " +
                                "WHERE u.id = :userId AND r.code = :roleCode",
                        UserRole.class
                )
                .setParameter("userId", userId)
                .setParameter("roleCode", roleCode)
                .getResultStream()
                .findFirst();
    }

    public void delete(UserRole userRole) {
        em.remove(em.contains(userRole) ? userRole : em.merge(userRole));
    }

    public List<Long> findUserIdsByRoleCode(String roleCode) {
        return em.createQuery(
                        "SELECT DISTINCT ur.user.id FROM UserRole ur " +
                                "JOIN ur.user u " +
                                "WHERE ur.role.code = :roleCode " +
                                "AND u.deletedAt IS NULL " +
                                "ORDER BY ur.user.id",
                        Long.class
                )
                .setParameter("roleCode", roleCode)
                .getResultList();
    }
}
