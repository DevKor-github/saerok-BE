package org.devkor.apu.saerok_server.domain.announcement.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.entity.Announcement;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.entity.AnnouncementStatus;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.repository.AnnouncementRepository;
import org.devkor.apu.saerok_server.domain.announcement.api.dto.response.AnnouncementDetailResponse;
import org.devkor.apu.saerok_server.domain.announcement.api.dto.response.AnnouncementListResponse;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AnnouncementQueryService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementListResponse listPublished() {
        List<Announcement> published = announcementRepository.findPublishedOrderByPublishedAtDesc();
        List<AnnouncementListResponse.Item> items = published.stream()
                .map(a -> new AnnouncementListResponse.Item(
                        a.getId(),
                        a.getTitle(),
                        OffsetDateTimeLocalizer.toSeoulLocalDateTime(a.getPublishedAt())
                ))
                .toList();

        return new AnnouncementListResponse(items);
    }

    public AnnouncementDetailResponse getPublishedAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 공지사항이에요."));

        if (announcement.getStatus() != AnnouncementStatus.PUBLISHED) {
            throw new NotFoundException("존재하지 않는 공지사항이에요.");
        }

        return new AnnouncementDetailResponse(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(announcement.getPublishedAt())
        );
    }
}
