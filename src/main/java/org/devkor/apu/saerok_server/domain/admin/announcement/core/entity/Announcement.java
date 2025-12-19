package org.devkor.apu.saerok_server.domain.admin.announcement.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "announcement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Announcement extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private User admin;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AnnouncementStatus status;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnnouncementImage> images = new ArrayList<>();

    private Announcement(User admin,
                         String title,
                         String content,
                         AnnouncementStatus status,
                         OffsetDateTime scheduledAt,
                         OffsetDateTime publishedAt) {
        this.admin = admin;
        this.title = title;
        this.content = content;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.publishedAt = publishedAt;
    }

    public static Announcement createScheduled(User admin,
                                               String title,
                                               String content,
                                               OffsetDateTime scheduledAt) {
        return new Announcement(admin, title, content, AnnouncementStatus.SCHEDULED, scheduledAt, null);
    }

    public static Announcement createPublished(User admin,
                                               String title,
                                               String content,
                                               OffsetDateTime publishedAt) {
        return new Announcement(admin, title, content, AnnouncementStatus.PUBLISHED, null, publishedAt);
    }

    public void publish(OffsetDateTime publishedAt) {
        if (this.status == AnnouncementStatus.PUBLISHED) {
            return;
        }
        this.status = AnnouncementStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        this.scheduledAt = null;
    }

    public void reschedule(OffsetDateTime newSchedule) {
        if (this.status == AnnouncementStatus.PUBLISHED) {
            throw new IllegalStateException("이미 게시된 공지사항은 수정할 수 없어요.");
        }
        this.scheduledAt = newSchedule;
    }

    public void updateContent(String title, String content) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
    }

    public void replaceImages(List<AnnouncementImage> newImages) {
        this.images.clear();
        if (newImages == null) return;
        for (AnnouncementImage image : newImages) {
            image.assignAnnouncement(this);
            this.images.add(image);
        }
    }

    public boolean isPublished() {
        return this.status == AnnouncementStatus.PUBLISHED;
    }
}
