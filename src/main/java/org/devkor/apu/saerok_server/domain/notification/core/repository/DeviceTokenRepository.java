package org.devkor.apu.saerok_server.domain.notification.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DeviceToken;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DeviceTokenRepository {

    private final EntityManager em;

    // 디바이스 토큰 저장
    public void save(DeviceToken deviceToken) { em.persist(deviceToken); }

    // 특정 토큰 삭제
    public void deleteByUserIdAndDeviceId(Long userId, String deviceId) {
        em.createQuery("DELETE FROM DeviceToken dt WHERE dt.user.id = :userId AND dt.deviceId = :deviceId")
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .executeUpdate();
    }

    // 모든 토큰 삭제
    public void deleteAllByUserId(Long userId) {
        em.createQuery("DELETE FROM DeviceToken dt WHERE dt.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    // 만료된 특정 토큰 삭제
    public void deleteByToken(String token) {
        em.createQuery("DELETE FROM DeviceToken dt WHERE dt.token = :token")
                .setParameter("token", token)
                .executeUpdate();
    }

    // 사용자 ID와 device_id로 디바이스 토큰 조회
    public Optional<DeviceToken> findByUserIdAndDeviceId(Long userId, String deviceId) {
        List<DeviceToken> results = em.createQuery(
                "SELECT dt FROM DeviceToken dt " +
                "WHERE dt.user.id = :userId AND dt.deviceId = :deviceId", DeviceToken.class)
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .setMaxResults(1)
                .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    // 사용자의 활성 토큰 조회 (푸쉬 알림 메시지 전송용)
    public List<DeviceToken> findActiveTokensByUserId(Long userId) {
        return em.createQuery(
                "SELECT dt FROM DeviceToken dt " +
                "WHERE dt.user.id = :userId AND dt.isActive = true " +
                "ORDER BY dt.createdAt DESC", DeviceToken.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    // 여러 사용자의 활성화된 디바이스 토큰들을 조회 (푸시 브로드캐스트 최적화용)
    public List<DeviceToken> findAllActiveTokensByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }

        return em.createQuery(
                        "SELECT dt FROM DeviceToken dt " +
                                "WHERE dt.user.id IN :userIds AND dt.isActive = true " +
                                "ORDER BY dt.user.id, dt.createdAt DESC", DeviceToken.class)
                .setParameter("userIds", userIds)
                .getResultList();
    }
}
