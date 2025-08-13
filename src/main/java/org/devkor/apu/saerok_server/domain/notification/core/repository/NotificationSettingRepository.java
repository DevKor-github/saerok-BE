package org.devkor.apu.saerok_server.domain.notification.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSetting;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationSettingRepository {

    private final EntityManager em;

    /** 특정 userDeviceId의 모든 설정 조회 */
    public List<NotificationSetting> findByUserDeviceId(Long userDeviceId) {
        return em.createQuery("""
                SELECT ns
                FROM NotificationSetting ns
                WHERE ns.userDevice.id = :userDeviceId
                """, NotificationSetting.class)
                .setParameter("userDeviceId", userDeviceId)
                .getResultList();
    }

    /** subject와 action(Nullable)을 함께 매칭하여 단건 조회 */
    public Optional<NotificationSetting> findByUserDeviceIdAndSubjectAndAction(
            Long userDeviceId, NotificationSubject subject, NotificationAction action
    ) {
        return em.createQuery("""
                SELECT ns
                FROM NotificationSetting ns
                WHERE ns.userDevice.id = :userDeviceId
                  AND ns.subject = :subject
                  AND ( (:action IS NULL AND ns.action IS NULL) OR ns.action = :action )
                """, NotificationSetting.class)
                .setParameter("userDeviceId", userDeviceId)
                .setParameter("subject", subject)
                .setParameter("action", action)
                .getResultStream()
                .findFirst();
    }

    /** 특정 userId에서 subject/action(Nullable) 조건으로 전체 조회 */
    public List<NotificationSetting> findForSubjectAndAction(
            Long userId, NotificationSubject subject, NotificationAction action
    ) {
        return em.createQuery("""
            SELECT ns
            FROM NotificationSetting ns
            WHERE ns.userDevice.user.id = :userId
              AND ns.subject = :subject
              AND ( (:action IS NULL AND ns.action IS NULL) OR ns.action = :action )
            """, NotificationSetting.class)
                .setParameter("userId", userId)
                .setParameter("subject", subject)
                .setParameter("action", action)
                .getResultList();
    }


    /** userId로 전체 설정 삭제 (토큰 전체 삭제 시 같이 사용) */
    public void deleteByUserId(Long userId) {
        em.createQuery("""
                DELETE FROM NotificationSetting ns
                WHERE ns.userDevice.user.id = :userId
                """)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    /** 특정 userDeviceId로 전체 설정 삭제 */
    public void deleteByUserDeviceId(Long userDeviceId) {
        em.createQuery("""
                DELETE FROM NotificationSetting ns
                WHERE ns.userDevice.id = :userDeviceId
                """)
                .setParameter("userDeviceId", userDeviceId)
                .executeUpdate();
    }

    public void saveAll(List<NotificationSetting> list) {
        list.forEach(em::persist);
    }
}
