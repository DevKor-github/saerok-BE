package org.devkor.apu.saerok_server.global.security.permission;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepository {

    private final EntityManager em;

    public void save(Role role) {
        em.persist(role);
    }

    public Optional<Role> findByCode(String code) {
        return em.createQuery(
                        "SELECT r FROM Role r WHERE r.code = :code",
                        Role.class
                )
                .setParameter("code", code)
                .getResultStream()
                .findFirst();
    }

    public List<Role> findAll() {
        return em.createQuery(
                        "SELECT r FROM Role r ORDER BY r.code",
                        Role.class
                )
                .getResultList();
    }
}
