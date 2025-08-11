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
    public void flush() { em.flush(); }

    // 특정 토큰 삭제
    public void deleteByUserIdAndDeviceId(Long userId, String deviceId) {
        em.createQuery("DELETE FROM UserDevice ud WHERE ud.user.id = :userId AND ud.deviceId = :deviceId")
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .executeUpdate();
    }

    // 모든 토큰 삭제
    public void deleteAllByUserId(Long userId) {
        em.createQuery("DELETE FROM UserDevice ud WHERE ud.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    // 개별 토큰 삭제
    public void deleteByToken(String token) {
        em.createQuery("DELETE FROM UserDevice ud WHERE ud.token = :token")
                .setParameter("token", token)
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

    // 사용자 ID와 device_id로 디바이스 토큰 조회
    public Optional<UserDevice> findByUserIdAndDeviceId(Long userId, String deviceId) {
        List<UserDevice> results = em.createQuery(
                "SELECT ud FROM UserDevice ud " +
                "WHERE ud.user.id = :userId AND ud.deviceId = :deviceId", UserDevice.class)
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .setMaxResults(1)
                .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }
    
    // UserDevice ID 목록으로 FCM 토큰 목록 조회
    public List<String> findTokensByUserDeviceIds(List<Long> userDeviceIds) {
        if (userDeviceIds.isEmpty()) {
            return List.of();
        }
        
        return em.createQuery("SELECT ud.token FROM UserDevice ud WHERE ud.id IN :userDeviceIds", String.class)
                .setParameter("userDeviceIds", userDeviceIds)
                .getResultList();
    }
}
