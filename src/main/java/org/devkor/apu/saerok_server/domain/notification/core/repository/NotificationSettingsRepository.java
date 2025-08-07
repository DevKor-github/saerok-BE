package org.devkor.apu.saerok_server.domain.notification.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSettings;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationSettingsRepository {

    private final EntityManager em;

    public void save(NotificationSettings notificationSettings) { em.persist(notificationSettings); }

    // 사용자와 디바이스 ID로 알림 설정 조회
    public Optional<NotificationSettings> findByUserIdAndDeviceId(Long userId, String deviceId) {
        List<NotificationSettings> results = em.createQuery(
                "SELECT ns FROM NotificationSettings ns " +
                "WHERE ns.user.id = :userId AND ns.deviceId = :deviceId", NotificationSettings.class)
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .setMaxResults(1)
                .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    // 사용자와 디바이스 ID로 알림 설정 삭제
    public void deleteByUserIdAndDeviceId(Long userId, String deviceId) {
        em.createQuery(
                "DELETE FROM NotificationSettings ns " +
                "WHERE ns.user.id = :userId AND ns.deviceId = :deviceId")
                .setParameter("userId", userId)
                .setParameter("deviceId", deviceId)
                .executeUpdate();
    }

    // 사용자의 모든 알림 설정 삭제
    public void deleteByUserId(Long userId) {
        em.createQuery(
                "DELETE FROM NotificationSettings ns " +
                "WHERE ns.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    /**
     * 좋아요 알림이 활성화된 특정 사용자 설정 조회
     */
    public List<NotificationSettings> findByUserIdWithLikeNotificationEnabled(Long userId) {
        return em.createQuery(
                        "SELECT ns FROM NotificationSettings ns " +
                                "WHERE ns.user.id = :userId " +
                                "AND ns.likeEnabled = true", NotificationSettings.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * 댓글 알림이 활성화된 특정 사용자 설정 조회
     */
    public List<NotificationSettings> findByUserIdWithCommentNotificationEnabled(Long userId) {
        return em.createQuery(
                        "SELECT ns FROM NotificationSettings ns " +
                                "WHERE ns.user.id = :userId " +
                                "AND ns.commentEnabled = true", NotificationSettings.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * 동정 제안 알림이 활성화된 특정 사용자 설정 조회
     */
    public List<NotificationSettings> findByUserIdWithBirdIdSuggestionNotificationEnabled(Long userId) {
        return em.createQuery(
                        "SELECT ns FROM NotificationSettings ns " +
                                "WHERE ns.user.id = :userId " +
                                "AND ns.birdIdSuggestionEnabled = true", NotificationSettings.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * 시스템 알림이 활성화된 특정 사용자 설정 조회
     */
    public List<NotificationSettings> findByUserIdWithSystemNotificationEnabled(Long userId) {
        return em.createQuery(
                        "SELECT ns FROM NotificationSettings ns " +
                                "WHERE ns.user.id = :userId " +
                                "AND ns.systemEnabled = true", NotificationSettings.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * 특정 사용자의 특정 알림 유형이 활성화된 설정 조회 (발송용)
     */
    public List<NotificationSettings> findByUserIdWithNotificationEnabled(Long userId, NotificationType type) {
        return switch (type) {
            case LIKE -> findByUserIdWithLikeNotificationEnabled(userId);
            case COMMENT -> findByUserIdWithCommentNotificationEnabled(userId);
            case BIRD_ID_SUGGESTION -> findByUserIdWithBirdIdSuggestionNotificationEnabled(userId);
            case SYSTEM -> findByUserIdWithSystemNotificationEnabled(userId);
        };
    }
}
