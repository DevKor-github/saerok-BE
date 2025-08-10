package org.devkor.apu.saerok_server.domain.notification.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSettings;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class NotificationSettingsRepository {

    private final EntityManager em;

    public void save(NotificationSettings notificationSettings) { 
        em.persist(notificationSettings); 
    }
    
    public void saveAll(List<NotificationSettings> notificationSettings) {
        for (NotificationSettings setting : notificationSettings) {
            em.persist(setting);
        }
    }

    // 특정 사용자와 기기의 모든 알림 설정을 삭제합니다
    public void deleteByUserIdAndDeviceId(Long userId, String deviceId) {
        em.createQuery("DELETE FROM NotificationSettings ns " +
                        "WHERE ns.user.id = :userId AND ns.deviceId = :deviceId")
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .executeUpdate();
    }

    // 사용자의 모든 알림 설정 삭제
    public void deleteByUserId(Long userId) {
        em.createQuery("DELETE FROM NotificationSettings ns WHERE ns.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    // 특정 사용자와 기기의 특정 알림 타입 설정을 조회합니다
    public Optional<NotificationSettings> findByUserIdAndDeviceIdAndType(Long userId, String deviceId, NotificationType type) {
        List<NotificationSettings> results = em.createQuery(
                "SELECT ns FROM NotificationSettings ns " +
                "WHERE ns.user.id = :userId AND ns.deviceId = :deviceId AND ns.type = :type", 
                NotificationSettings.class)
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .setParameter("type", type)
                .setMaxResults(1)
                .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    // 특정 사용자와 기기의 모든 알림 설정을 조회합니다
    public List<NotificationSettings> findByUserIdAndDeviceId(Long userId, String deviceId) {
        return em.createQuery(
                "SELECT ns FROM NotificationSettings ns " +
                "WHERE ns.user.id = :userId AND ns.deviceId = :deviceId", 
                NotificationSettings.class)
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .getResultList();
    }

    // 특정 알림 타입이 활성화된 특정 사용자의 설정을 조회합니다
    // 한 사용자가 여러 개의 디바이스로 이용 중일 때 활성화된 기기에 모두 보내기 위해 사용합니다
    public List<NotificationSettings> findByUserIdAndTypeAndEnabledTrue(Long userId, NotificationType type) {
        return em.createQuery(
                "SELECT ns FROM NotificationSettings ns " +
                "WHERE ns.user.id = :userId AND ns.type = :type AND ns.enabled = true", 
                NotificationSettings.class)
                .setParameter("userId", userId)
                .setParameter("type", type)
                .getResultList();
    }
}
