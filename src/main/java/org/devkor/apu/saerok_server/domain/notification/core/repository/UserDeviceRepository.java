package org.devkor.apu.saerok_server.domain.notification.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DevicePlatform;
import org.devkor.apu.saerok_server.domain.notification.core.entity.UserDevice;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDeviceRepository {

    private final EntityManager em;

    // 디바이스 토큰 저장
    public void save(UserDevice userDevice) { em.persist(userDevice); }
    public void flush() { em.flush(); }

    public int deactivateByTokens(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return 0;
        }

        return em.createQuery("""
                UPDATE UserDevice ud
                   SET ud.token = NULL,
                       ud.updatedAt = CURRENT_TIMESTAMP
                 WHERE ud.token IN :tokens
                """)
                .setParameter("tokens", tokens)
                .executeUpdate();
    }

    public int deactivateConflictingTokensForRegistration(Long userId, String deviceId,
                                                          DevicePlatform platform, String token) {
        return em.createQuery("""
                UPDATE UserDevice ud
                   SET ud.token = NULL,
                       ud.updatedAt = CURRENT_TIMESTAMP
                 WHERE ud.token IS NOT NULL
                   AND (
                           ud.token = :token
                        OR (ud.deviceId = :deviceId AND ud.platform = :platform)
                       )
                   AND NOT (
                           ud.user.id = :userId
                       AND ud.deviceId = :deviceId
                       AND ud.platform = :platform
                   )
                """)
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .setParameter("platform", platform)
                .setParameter("token", token)
                .executeUpdate();
    }

    // 회원 탈퇴 시에만 사용하는 영구 삭제
    public int deleteByUserId(Long userId) {
        return em.createQuery("DELETE FROM UserDevice ud WHERE ud.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    // ID로 디바이스 조회
    public Optional<UserDevice> findById(Long id) {
        List<UserDevice> results = em.createQuery("SELECT ud FROM UserDevice ud WHERE ud.id = :id", UserDevice.class)
                .setParameter("id", id)
                .setMaxResults(1)
                .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    // 사용자 ID, device_id, platform으로 디바이스 조회
    public Optional<UserDevice> findByUserIdAndDeviceIdAndPlatform(Long userId, String deviceId, DevicePlatform platform) {
        List<UserDevice> results = em.createQuery(
                "SELECT ud FROM UserDevice ud " +
                "WHERE ud.user.id = :userId AND ud.deviceId = :deviceId AND ud.platform = :platform", UserDevice.class)
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .setParameter("platform", platform)
                .setMaxResults(1)
                .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    public List<UserDevice> findAllActiveByUserId(Long userId) {
        return em.createQuery("""
                SELECT ud
                  FROM UserDevice ud
                 WHERE ud.user.id = :userId
                   AND ud.token IS NOT NULL
                """, UserDevice.class)
                .setParameter("userId", userId)
                .getResultList();
    }
    
    // UserDevice ID 목록으로 FCM 토큰 목록 조회
    public List<String> findTokensByUserDeviceIds(List<Long> userDeviceIds) {
        if (userDeviceIds.isEmpty()) {
            return List.of();
        }
        
        return em.createQuery("""
                SELECT ud.token
                  FROM UserDevice ud
                 WHERE ud.id IN :userDeviceIds
                   AND ud.token IS NOT NULL
                """, String.class)
                .setParameter("userDeviceIds", userDeviceIds)
                .getResultList();
    }
    
    // 사용자 ID로 모든 FCM 토큰 조회 (사일런트 푸시용)
    public List<String> findTokensByUserId(Long userId) {
        return em.createQuery("""
                SELECT ud.token
                  FROM UserDevice ud
                 WHERE ud.user.id = :userId
                   AND ud.token IS NOT NULL
                """, String.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
