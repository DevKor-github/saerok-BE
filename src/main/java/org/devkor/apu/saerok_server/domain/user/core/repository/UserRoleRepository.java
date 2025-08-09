package org.devkor.apu.saerok_server.domain.user.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRole;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRoleRepository {

    private final EntityManager em;

    public void save(UserRole userRole) {
        em.persist(userRole);
    }

    public List<UserRole> findByUser(User user) {
        return em.createQuery("SELECT ur FROM UserRole ur WHERE ur.user = :user", UserRole.class)
                .setParameter("user", user)
                .getResultList();
    }

    public void deleteByUserId(Long userId) {
        em.createQuery("DELETE FROM UserRole ur WHERE ur.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
