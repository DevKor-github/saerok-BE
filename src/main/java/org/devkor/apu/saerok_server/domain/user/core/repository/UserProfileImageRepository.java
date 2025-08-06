package org.devkor.apu.saerok_server.domain.user.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserProfileImage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserProfileImageRepository {

    private final EntityManager em;

    public void save(UserProfileImage profileImage) {
        em.persist(profileImage);
    }

    public void flush() {
        em.flush();
    }

    public Optional<UserProfileImage> findByUserId(Long userId) {
        return em.createQuery("SELECT p FROM UserProfileImage p WHERE p.user.id = :userId", UserProfileImage.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
    }

    public Optional<String> findObjectKeyByUserId(Long userId) {
        return em.createQuery("SELECT p.objectKey FROM UserProfileImage p WHERE p.user.id = :userId", String.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
    }

    /**
     * 여러 사용자 ID로 프로필 이미지 Object Key를 일괄 조회
     * 존재하지 않는 경우 null로 매핑
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

        // (1) 먼저 결과를 Map<Long, String>으로 변환
        Map<Long, String> objectKeyMap = results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (String) result[1]
                ));

        // (2) 조회되지 않은 userId에 대해 (userId, null) 삽입
        for (Long userId : userIds) {
            objectKeyMap.putIfAbsent(userId, null);
        }

        return objectKeyMap;
    }

    public void remove(UserProfileImage profileImage) { 
        em.remove(profileImage); 
    }
}
