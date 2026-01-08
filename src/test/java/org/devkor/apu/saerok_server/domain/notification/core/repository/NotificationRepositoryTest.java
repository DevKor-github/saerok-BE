package org.devkor.apu.saerok_server.domain.notification.core.repository;

import org.devkor.apu.saerok_server.domain.notification.core.entity.Notification;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.UserBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(NotificationRepository.class)
@ActiveProfiles("test")
class NotificationRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired NotificationRepository repo;

    private User user() {
        return new UserBuilder(em).build();
    }

    private Notification noti(User user, NotificationType type, boolean isRead) {
        return Notification.builder()
                .user(user)
                .type(type)
                .isRead(isRead)
                .payload(Map.of("key", "value"))
                .build();
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("save / findById")
    void save_findById() {
        User u = user();
        Notification n = noti(u, NotificationType.COMMENTED_ON_COLLECTION, false);
        repo.save(n);
        em.flush(); em.clear();

        Optional<Notification> found = repo.findById(n.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(u.getId());
        assertThat(found.get().getType()).isEqualTo(NotificationType.COMMENTED_ON_COLLECTION);
        assertThat(found.get().getIsRead()).isFalse();
    }

    @Test @DisplayName("findById - 존재하지 않음")
    void findById_notExists() {
        Optional<Notification> found = repo.findById(99999L);
        assertThat(found).isEmpty();
    }

    @Test @DisplayName("remove")
    void remove() {
        User u = user();
        Notification n = noti(u, NotificationType.LIKED_ON_COLLECTION, false);
        repo.save(n);
        em.flush(); em.clear();

        Notification toDelete = em.find(Notification.class, n.getId());
        repo.remove(toDelete);
        em.flush(); em.clear();

        Optional<Notification> found = repo.findById(n.getId());
        assertThat(found).isEmpty();
    }

    @Test @DisplayName("findByUserId - 최신순 정렬")
    void findByUserId() throws InterruptedException {
        User u1 = user();
        User u2 = user();

        Notification n1 = noti(u1, NotificationType.COMMENTED_ON_COLLECTION, false);
        repo.save(n1);
        em.flush();

        Thread.sleep(10);

        Notification n2 = noti(u1, NotificationType.LIKED_ON_COLLECTION, false);
        repo.save(n2);
        Notification n3 = noti(u2, NotificationType.COMMENTED_ON_COLLECTION, false);
        repo.save(n3);
        em.flush(); em.clear();

        List<Notification> u1Notis = repo.findByUserId(u1.getId());
        List<Notification> u2Notis = repo.findByUserId(u2.getId());

        assertThat(u1Notis).hasSize(2);
        assertThat(u1Notis.get(0).getId()).isEqualTo(n2.getId());
        assertThat(u1Notis.get(1).getId()).isEqualTo(n1.getId());
        assertThat(u2Notis).hasSize(1);
        assertThat(u2Notis.getFirst().getId()).isEqualTo(n3.getId());
    }

    @Test @DisplayName("findByUserId - 빈 리스트")
    void findByUserId_empty() {
        User u = user();
        em.flush(); em.clear();

        List<Notification> notis = repo.findByUserId(u.getId());
        assertThat(notis).isEmpty();
    }

    @Test @DisplayName("countUnreadByUserId")
    void countUnreadByUserId() {
        User u = user();
        repo.save(noti(u, NotificationType.COMMENTED_ON_COLLECTION, false));
        repo.save(noti(u, NotificationType.LIKED_ON_COLLECTION, false));
        repo.save(noti(u, NotificationType.COMMENTED_ON_COLLECTION, true));
        em.flush(); em.clear();

        Long unreadCount = repo.countUnreadByUserId(u.getId());
        assertThat(unreadCount).isEqualTo(2L);
    }

    @Test @DisplayName("countUnreadByUserId - 모두 읽음")
    void countUnreadByUserId_allRead() {
        User u = user();
        repo.save(noti(u, NotificationType.COMMENTED_ON_COLLECTION, true));
        repo.save(noti(u, NotificationType.LIKED_ON_COLLECTION, true));
        em.flush(); em.clear();

        Long unreadCount = repo.countUnreadByUserId(u.getId());
        assertThat(unreadCount).isEqualTo(0L);
    }

    @Test @DisplayName("markAllAsReadByUserId")
    void markAllAsReadByUserId() {
        User u = user();
        repo.save(noti(u, NotificationType.COMMENTED_ON_COLLECTION, false));
        repo.save(noti(u, NotificationType.LIKED_ON_COLLECTION, false));
        repo.save(noti(u, NotificationType.REPLIED_TO_COMMENT, false));
        em.flush(); em.clear();

        repo.markAllAsReadByUserId(u.getId());
        em.flush(); em.clear();

        Long unreadCount = repo.countUnreadByUserId(u.getId());
        assertThat(unreadCount).isEqualTo(0L);

        List<Notification> notis = repo.findByUserId(u.getId());
        assertThat(notis).allMatch(Notification::getIsRead);
    }

    @Test @DisplayName("markAllAsReadByUserId - 이미 읽음")
    void markAllAsReadByUserId_alreadyRead() {
        User u = user();
        repo.save(noti(u, NotificationType.COMMENTED_ON_COLLECTION, true));
        em.flush(); em.clear();

        repo.markAllAsReadByUserId(u.getId());
        em.flush(); em.clear();

        Long unreadCount = repo.countUnreadByUserId(u.getId());
        assertThat(unreadCount).isEqualTo(0L);
    }

    @Test @DisplayName("deleteByUserId")
    void deleteByUserId() {
        User u1 = user();
        User u2 = user();
        repo.save(noti(u1, NotificationType.COMMENTED_ON_COLLECTION, false));
        repo.save(noti(u1, NotificationType.LIKED_ON_COLLECTION, false));
        repo.save(noti(u2, NotificationType.COMMENTED_ON_COLLECTION, false));
        em.flush(); em.clear();

        int deletedCount = repo.deleteByUserId(u1.getId());
        em.flush(); em.clear();

        assertThat(deletedCount).isEqualTo(2);
        assertThat(repo.findByUserId(u1.getId())).isEmpty();
        assertThat(repo.findByUserId(u2.getId())).hasSize(1);
    }

    @Test @DisplayName("deleteByUserId - 알림 없음")
    void deleteByUserId_noNotifications() {
        User u = user();
        em.flush(); em.clear();

        int deletedCount = repo.deleteByUserId(u.getId());
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test @DisplayName("markAsRead")
    void markAsRead() {
        User u = user();
        Notification n = noti(u, NotificationType.COMMENTED_ON_COLLECTION, false);
        repo.save(n);
        em.flush(); em.clear();

        Notification found = repo.findById(n.getId()).orElseThrow();
        found.markAsRead();
        em.flush(); em.clear();

        Notification updated = repo.findById(n.getId()).orElseThrow();
        assertThat(updated.getIsRead()).isTrue();
    }

    @Test @DisplayName("Notification Builder - payload 방어적 복사")
    void notification_builder_defensiveCopy() {
        User u = user();
        Map<String, Object> payload = Map.of("key1", "value1");

        Notification n = Notification.builder()
                .user(u)
                .type(NotificationType.COMMENTED_ON_COLLECTION)
                .payload(payload)
                .build();
        repo.save(n);
        em.flush();

        assertThat(n.getPayload()).isNotSameAs(payload);
        assertThat(n.getPayload()).containsEntry("key1", "value1");
    }
}
