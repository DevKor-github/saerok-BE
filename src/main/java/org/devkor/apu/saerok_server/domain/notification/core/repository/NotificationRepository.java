package org.devkor.apu.saerok_server.domain.notification.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.core.entity.Notification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {

    private final EntityManager em;

    public Optional<Notification> findById(Long id) {
        return Optional.ofNullable(em.find(Notification.class, id));
    }

    public void save(Notification notification) { em.persist(notification); }

    public void remove(Notification notification) { em.remove(notification); }

    // 사용자의 모든 알림 삭제
    public void deleteAllByUserId(Long userId) {
        em.createQuery("DELETE FROM Notification n WHERE n.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    // 사용자의 알림 목록 조회
    public List<Notification> findByUserId(Long userId) {
        return em.createQuery(
                "SELECT n FROM Notification n " +
                "WHERE n.user.id = :userId " +
                "ORDER BY n.createdAt DESC", Notification.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    // 사용자의 읽지 않은 알림 목록 조회
    public List<Notification> findUnreadByUserId(Long userId) {
        return em.createQuery(
                "SELECT n FROM Notification n " +
                "WHERE n.user.id = :userId AND n.isRead = false " +
                "ORDER BY n.createdAt DESC", Notification.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    // 사용자의 읽지 않은 알림 개수 조회
    public Long countUnreadByUserId(Long userId) {
        return em.createQuery(
                "SELECT COUNT(n) FROM Notification n " +
                "WHERE n.user.id = :userId AND n.isRead = false", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    // 사용자의 모든 알림을 읽음 처리
    public int markAllAsReadByUserId(Long userId) {
        return em.createQuery(
                "UPDATE Notification n SET n.isRead = true " +
                "WHERE n.user.id = :userId AND n.isRead = false")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    // TODO: 컬렉션 삭제 시 CollectionCommandService에서 호출 필요
    /**
     * 특정 컬렉션과 관련된 모든 알림 삭제 (컬렉션 삭제 시 사용)
     */
    public void deleteByRelatedId(Long relatedId) {
        em.createQuery(
                "DELETE FROM Notification n " +
                "WHERE n.relatedId = :relatedId")
                .setParameter("relatedId", relatedId)
                .executeUpdate();
    }
}
