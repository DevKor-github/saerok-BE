package org.devkor.apu.saerok_server.global.security.permission;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RolePermissionRepository {

    private final EntityManager em;

    public void save(RolePermission rolePermission) {
        em.persist(rolePermission);
    }

    public List<RolePermission> findByRole(Role role) {
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

    public List<RolePermission> findByRoleCode(String roleCode) {
        return em.createQuery(
                        "SELECT rp FROM RolePermission rp " +
                                "JOIN FETCH rp.permission p " +
                                "JOIN rp.role r " +
                                "WHERE r.code = :code " +
                                "ORDER BY p.key",
                        RolePermission.class
                )
                .setParameter("code", roleCode)
                .getResultList();
    }

    public List<RolePermission> findAll() {
        return em.createQuery(
                        "SELECT rp FROM RolePermission rp " +
                                "JOIN FETCH rp.permission p " +
                                "JOIN FETCH rp.role r " +
                                "ORDER BY r.code, p.key",
                        RolePermission.class
                )
                .getResultList();
    }

    public void deleteByRole(Role role) {
        em.createQuery("DELETE FROM RolePermission rp WHERE rp.role = :role")
                .setParameter("role", role)
                .executeUpdate();
    }
}
