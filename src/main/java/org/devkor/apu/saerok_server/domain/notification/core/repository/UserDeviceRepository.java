package org.devkor.apu.saerok_server.domain.notification.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
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

    // 특정 토큰 삭제
    public void deleteByUserIdAndDeviceId(Long userId, String deviceId) {
        em.createQuery("DELETE FROM UserDevice dt WHERE dt.user.id = :userId AND dt.deviceId = :deviceId")
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .executeUpdate();
    }

    // 모든 토큰 삭제
    public void deleteAllByUserId(Long userId) {
        em.createQuery("DELETE FROM UserDevice dt WHERE dt.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    // 개별 토큰 삭제
    public void deleteByToken(String token) {
        em.createQuery("DELETE FROM UserDevice dt WHERE dt.token = :token")
                .setParameter("token", token)
                .executeUpdate();
    }

    // 사용자 ID와 device_id로 디바이스 토큰 조회
    public Optional<UserDevice> findByUserIdAndDeviceId(Long userId, String deviceId) {
        List<UserDevice> results = em.createQuery(
                "SELECT dt FROM UserDevice dt " +
                "WHERE dt.user.id = :userId AND dt.deviceId = :deviceId", UserDevice.class)
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .setMaxResults(1)
                .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    // 특정 사용자의 특정 디바이스들의 토큰 조회
    public List<UserDevice> findByUserIdAndDeviceIds(Long userId, List<String> deviceIds) {
        if (deviceIds.isEmpty()) {
            return List.of();
        }
        return em.createQuery(
                "SELECT dt FROM UserDevice dt " +
                "WHERE dt.user.id = :userId AND dt.deviceId IN :deviceIds " +
                "ORDER BY dt.createdAt DESC", UserDevice.class)
                .setParameter("userId", userId)
                .setParameter("deviceIds", deviceIds)
                .getResultList();
    }
}
