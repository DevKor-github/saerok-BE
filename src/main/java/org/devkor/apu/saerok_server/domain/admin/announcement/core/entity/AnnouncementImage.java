package org.devkor.apu.saerok_server.domain.admin.announcement.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.global.shared.entity.CreatedAtOnly;

@Entity
@Table(name = "announcement_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnnouncementImage extends CreatedAtOnly {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    private Announcement announcement;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    private AnnouncementImage(String objectKey, String contentType) {
        this.objectKey = objectKey;
        this.contentType = contentType;
    }

    public static AnnouncementImage of(String objectKey, String contentType) {
        return new AnnouncementImage(objectKey, contentType);
    }

    void assignAnnouncement(Announcement announcement) {
        this.announcement = announcement;
    }
}
