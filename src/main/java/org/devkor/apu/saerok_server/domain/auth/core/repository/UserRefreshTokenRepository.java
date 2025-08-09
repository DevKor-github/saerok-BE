package org.devkor.apu.saerok_server.domain.auth.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.core.entity.UserRefreshToken;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRefreshTokenRepository {

    private final EntityManager em;

    public void save(UserRefreshToken userRefreshToken) {
        em.persist(userRefreshToken);
    }

    public Optional<UserRefreshToken> findByRefreshTokenHash(String refreshTokenHash) {
        return em.createQuery("SELECT rt FROM UserRefreshToken rt WHERE rt.refreshTokenHash = :refreshTokenHash", UserRefreshToken.class)
                .setParameter("refreshTokenHash", refreshTokenHash)
                .getResultStream()
                .findFirst();
    }

    public void deleteByUserId(Long userId) {
        em.createQuery("DELETE FROM UserRefreshToken rt WHERE rt.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
