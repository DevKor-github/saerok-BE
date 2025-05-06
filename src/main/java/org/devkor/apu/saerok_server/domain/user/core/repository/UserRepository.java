package org.devkor.apu.saerok_server.domain.user.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.springframework.stereotype.Repository;

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
}
