package org.devkor.apu.saerok_server.domain.announcement.application;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnnouncementPublicationScheduler {

    private final AnnouncementPublicationService publicationService;

    @Scheduled(fixedDelayString = "10000", initialDelayString = "10000")
    public void publishDueAnnouncements() {
        publicationService.publishDueAnnouncements();
    }
}
