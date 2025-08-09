package org.devkor.apu.saerok_server.domain.auth.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialAuth;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SocialAuthRepository {

    private final EntityManager em;

    public Optional<SocialAuth> findByProviderAndProviderUserId(SocialProviderType provider, String providerUserId) {
        return em.createQuery(
                "SELECT s FROM SocialAuth s WHERE s.provider = :provider AND s.providerUserId = :providerUserId", SocialAuth.class)
                .setParameter("provider", provider)
                .setParameter("providerUserId", providerUserId)
                .getResultStream()
                .findFirst();
    }

    public SocialAuth save(SocialAuth socialAuth) {
        em.persist(socialAuth);
        return socialAuth;
    }

    public List<SocialAuth> findByUserId(Long userId) {
        return em.createQuery(
                "SELECT s FROM SocialAuth s WHERE s.user.id = :userId", SocialAuth.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public void deleteByUserId(Long userId) {
        em.createQuery("DELETE FROM SocialAuth s WHERE s.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
