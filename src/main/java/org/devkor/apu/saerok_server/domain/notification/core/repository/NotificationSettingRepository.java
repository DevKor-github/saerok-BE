package org.devkor.apu.saerok_server.domain.notification.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationSettingRepository {

    private final EntityManager em;

    public void save(NotificationSetting notificationSetting) {
        em.persist(notificationSetting);
    }
    
    public void saveAll(List<NotificationSetting> notificationSettings) {
        for (NotificationSetting setting : notificationSettings) {
            em.persist(setting);
        }
    }

    // 특정 사용자와 기기의 모든 알림 설정을 삭제합니다
    public void deleteByUserIdAndDeviceId(Long userId, String deviceId) {
        em.createQuery("DELETE FROM NotificationSetting ns " +
                        "WHERE ns.user.id = :userId AND ns.deviceId = :deviceId")
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .executeUpdate();
    }

    // 사용자의 모든 알림 설정 삭제
    public void deleteByUserId(Long userId) {
        em.createQuery("DELETE FROM NotificationSetting ns WHERE ns.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    // 특정 사용자와 기기의 특정 알림 타입 설정을 조회합니다
    public Optional<NotificationSetting> findByUserIdAndDeviceIdAndType(Long userId, String deviceId, NotificationType type) {
        List<NotificationSetting> results = em.createQuery(
                "SELECT ns FROM NotificationSetting ns " +
                "WHERE ns.user.id = :userId AND ns.deviceId = :deviceId AND ns.type = :type", 
                NotificationSetting.class)
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .setParameter("type", type)
                .setMaxResults(1)
                .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    // 특정 사용자와 기기의 모든 알림 설정을 조회합니다
    public List<NotificationSetting> findByUserIdAndDeviceId(Long userId, String deviceId) {
        return em.createQuery(
                "SELECT ns FROM NotificationSetting ns " +
                "WHERE ns.user.id = :userId AND ns.deviceId = :deviceId", 
                NotificationSetting.class)
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .getResultList();
    }

    // 특정 알림 타입이 활성화된 특정 사용자의 설정을 조회합니다
    // 한 사용자가 여러 개의 디바이스로 이용 중일 때 활성화된 기기에 모두 보내기 위해 사용합니다
    public List<NotificationSetting> findByUserIdAndTypeAndEnabledTrue(Long userId, NotificationType type) {
        return em.createQuery(
                "SELECT ns FROM NotificationSetting ns " +
                "WHERE ns.user.id = :userId AND ns.type = :type AND ns.enabled = true", 
                NotificationSetting.class)
                .setParameter("userId", userId)
                .setParameter("type", type)
                .getResultList();
    }
}
