package org.devkor.apu.saerok_server.domain.admin.announcement.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.entity.Announcement;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.entity.AnnouncementStatus;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AnnouncementRepository {

    private final EntityManager em;

    public Announcement save(Announcement announcement) {
        em.persist(announcement);
        return announcement;
    }

    public Optional<Announcement> findById(Long id) {
        List<Announcement> results = em.createQuery(
                        "SELECT DISTINCT a FROM Announcement a " +
                                "JOIN FETCH a.admin " +
                                "LEFT JOIN FETCH a.images " +
                                "WHERE a.id = :id",
                        Announcement.class)
                .setParameter("id", id)
                .getResultList();

        return results.stream().findFirst();
    }

    public List<Announcement> findAllOrderByLatest() {
        return em.createQuery(
                        "SELECT a FROM Announcement a " +
                                "JOIN FETCH a.admin " +
                                "ORDER BY COALESCE(a.publishedAt, a.scheduledAt) DESC, a.id DESC",
                        Announcement.class)
                .getResultList();
    }

    public List<Announcement> findPublishedOrderByPublishedAtDesc() {
        return em.createQuery(
                        "SELECT a FROM Announcement a " +
                                "WHERE a.status = :status " +
                                "ORDER BY a.publishedAt DESC, a.id DESC",
                        Announcement.class)
                .setParameter("status", AnnouncementStatus.PUBLISHED)
                .getResultList();
    }

    public List<Announcement> findDueAnnouncements(OffsetDateTime now) {
        return em.createQuery(
                        "SELECT a FROM Announcement a " +
                                "WHERE a.status = :status AND a.scheduledAt <= :now",
                        Announcement.class)
                .setParameter("status", AnnouncementStatus.SCHEDULED)
                .setParameter("now", now)
                .getResultList();
    }

    public void delete(Announcement announcement) {
        em.remove(announcement);
    }
}
