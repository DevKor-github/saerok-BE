package org.devkor.apu.saerok_server.domain.announcement.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.entity.Announcement;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.repository.AnnouncementRepository;
import org.devkor.apu.saerok_server.domain.announcement.application.event.AnnouncementPublishedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AnnouncementPublicationService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final AnnouncementRepository announcementRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OffsetDateTime toKstOffset(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.atZone(KST).toOffsetDateTime();
    }

    public OffsetDateTime nowKst() {
        return OffsetDateTime.now(KST);
    }

    public void publishDueAnnouncements() {
        OffsetDateTime now = nowKst();
        List<Announcement> dueAnnouncements = announcementRepository.findDueAnnouncements(now);

        for (Announcement announcement : dueAnnouncements) {
            OffsetDateTime publishedAt = announcement.getScheduledAt() != null
                    ? announcement.getScheduledAt()
                    : now;
            publishAnnouncement(announcement, publishedAt);
        }
    }

    public void publishAnnouncement(Announcement announcement, OffsetDateTime publishedAt) {
        if (announcement.isPublished()) {
            return;
        }
        announcement.publish(publishedAt);
        notifyPublishedAnnouncement(announcement);
    }

    public void notifyPublishedAnnouncement(Announcement announcement) {
        if (!announcement.isPublished() || !Boolean.TRUE.equals(announcement.getSendNotification())) {
            return;
        }
        eventPublisher.publishEvent(new AnnouncementPublishedEvent(announcement.getId()));
    }
}
