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

    public List<NotificationSetting> findByUserDeviceId(Long userDeviceId) {
        return em.createQuery("""
                select ns
                  from NotificationSetting ns
                 where ns.userDevice.id = :deviceId
                """, NotificationSetting.class)
                .setParameter("deviceId", userDeviceId)
                .getResultList();
    }

    public Optional<NotificationSetting> findByUserDeviceIdAndType(Long userDeviceId, NotificationType type) {
        List<NotificationSetting> list = em.createQuery("""
                select ns
                  from NotificationSetting ns
                 where ns.userDevice.id = :deviceId
                   and ns.type = :type
                """, NotificationSetting.class)
                .setParameter("deviceId", userDeviceId)
                .setParameter("type", type)
                .getResultList();
        return list.stream().findFirst();
    }

    /** 푸시 발송용: 유저의 디바이스 중에서 해당 type이 enabled=true 인 디바이스 id 목록 */
    public List<Long> findEnabledDeviceIdsByUserAndType(Long userId, NotificationType type) {
        return em.createQuery("""
                select ns.userDevice.id
                  from NotificationSetting ns
                 where ns.userDevice.user.id = :userId
                   and ns.type = :type
                   and ns.enabled = true
                """, Long.class)
                .setParameter("userId", userId)
                .setParameter("type", type)
                .getResultList();
    }

    public void save(NotificationSetting setting) {
        em.persist(setting);
    }

    public void saveAll(List<NotificationSetting> settings) {
        for (NotificationSetting s : settings) em.persist(s);
    }

    public void deleteByUserId(Long userId) {
        em.createQuery("""
            delete from NotificationSetting ns
             where ns.userDevice.user.id = :userId
        """).setParameter("userId", userId).executeUpdate();
    }
}
