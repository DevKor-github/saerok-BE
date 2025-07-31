package org.devkor.apu.saerok_server.domain.user.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserProfileImage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * 여러 사용자 ID로 프로필 이미지 Object Key를 일괄 조회
     */
    public Map<Long, String> findObjectKeysByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        
        List<Object[]> results = em.createQuery(
                "SELECT p.user.id, p.objectKey FROM UserProfileImage p WHERE p.user.id IN :userIds", 
                Object[].class)
                .setParameter("userIds", userIds)
                .getResultList();
        
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (Long) result[0],    // userId
                    result -> (String) result[1]   // objectKey
                ));
    }

    public void remove(UserProfileImage profileImage) { 
        em.remove(profileImage); 
    }
}
