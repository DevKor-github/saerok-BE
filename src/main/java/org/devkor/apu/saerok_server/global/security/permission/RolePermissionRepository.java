package org.devkor.apu.saerok_server.global.security.permission;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRoleType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RolePermissionRepository {

    private final EntityManager em;

    public void save(RolePermission rolePermission) {
        em.persist(rolePermission);
    }

    public List<RolePermission> findByRole(UserRoleType role) {
        return em.createQuery(
                        "SELECT rp FROM RolePermission rp " +
                                "JOIN FETCH rp.permission p " +
                                "WHERE rp.role = :role " +
                                "ORDER BY p.key",
                        RolePermission.class
                )
                .setParameter("role", role)
                .getResultList();
    }

    public List<RolePermission> findAll() {
        return em.createQuery(
                        "SELECT rp FROM RolePermission rp " +
                                "JOIN FETCH rp.permission p " +
                                "ORDER BY rp.role, p.key",
                        RolePermission.class
                )
                .getResultList();
    }
}
