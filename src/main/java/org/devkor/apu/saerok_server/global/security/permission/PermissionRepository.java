package org.devkor.apu.saerok_server.global.security.permission;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PermissionRepository {

    private final EntityManager em;

    public void save(Permission permission) {
        em.persist(permission);
    }

    public Optional<Permission> findByKey(PermissionKey key) {
        return em.createQuery(
                        "SELECT p FROM Permission p WHERE p.key = :key",
                        Permission.class
                )
                .setParameter("key", key)
                .getResultStream()
                .findFirst();
    }

    public List<Permission> findAll() {
        return em.createQuery(
                        "SELECT p FROM Permission p ORDER BY p.key",
                        Permission.class
                )
                .getResultList();
    }
}
