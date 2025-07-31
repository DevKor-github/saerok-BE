package org.devkor.apu.saerok_server.domain.user.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserProfileImage;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserProfileImageRepository {

    private final EntityManager em;

    public Long save(UserProfileImage profileImage) {
        em.persist(profileImage);
        return profileImage.getId();
    }

    public UserProfileImage findByUserId(Long userId) {
        return em.createQuery("SELECT p FROM UserProfileImage p WHERE p.user.id = :userId", UserProfileImage.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    public String findObjectKeyByUserId(Long userId) {
        return em.createQuery("SELECT p.objectKey FROM UserProfileImage p WHERE p.user.id = :userId", String.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    public void remove(UserProfileImage profileImage) { 
        em.remove(profileImage); 
    }
}
