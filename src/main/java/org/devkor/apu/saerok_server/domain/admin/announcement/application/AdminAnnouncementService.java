package org.devkor.apu.saerok_server.domain.admin.announcement.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.announcement.api.dto.request.AdminAnnouncementImageRequest;
import org.devkor.apu.saerok_server.domain.admin.announcement.api.dto.response.AnnouncementImagePresignResponse;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.entity.Announcement;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.entity.AnnouncementImage;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.entity.AnnouncementStatus;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.repository.AnnouncementRepository;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditAction;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditLog;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditTargetType;
import org.devkor.apu.saerok_server.domain.admin.audit.core.repository.AdminAuditLogRepository;
import org.devkor.apu.saerok_server.domain.announcement.application.AnnouncementPublicationService;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.image.ImageKind;
import org.devkor.apu.saerok_server.global.shared.image.ImageVariantService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.devkor.apu.saerok_server.global.shared.util.TransactionUtils.runAfterCommitOrNow;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminAnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final ImageVariantService imageVariantService;
    private final ImageDomainService imageDomainService;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final AnnouncementPublicationService publicationService;

    public Announcement createAnnouncement(Long adminUserId,
                                           String title,
                                           String content,
                                           LocalDateTime scheduledAt,
                                           Boolean publishNow,
                                           Boolean sendNotification,
                                           String pushTitle,
                                           String pushBody,
                                           String inAppBody,
                                           List<AdminAnnouncementImageRequest> images) {
        validateScheduleRequest(scheduledAt, publishNow);
        validateNotificationOptions(sendNotification, pushTitle, pushBody, inAppBody);

        User admin = loadAdmin(adminUserId);
        OffsetDateTime now = publicationService.nowKst();
        OffsetDateTime scheduled = publicationService.toKstOffset(scheduledAt);

        boolean shouldSendNotification = sendNotification;
        if (!shouldSendNotification) {
            pushTitle = null;
            pushBody = null;
            inAppBody = null;
        }

        Announcement announcement;
        if (Boolean.TRUE.equals(publishNow)) {
            announcement = Announcement.createPublished(admin, title, content, now,
                    shouldSendNotification, pushTitle, pushBody, inAppBody);
        } else {
            announcement = Announcement.createScheduled(admin, title, content, scheduled,
                    shouldSendNotification, pushTitle, pushBody, inAppBody);
        }

        announcement.replaceImages(toImages(images));
        Announcement saved = announcementRepository.save(announcement);

        if (Boolean.TRUE.equals(publishNow)) {
            publicationService.notifyPublishedAnnouncement(saved);
        } else if (scheduled != null && !scheduled.isAfter(now)) {
            publicationService.publishAnnouncement(saved, scheduled);
        }

        recordAudit(admin, AdminAuditAction.ANNOUNCEMENT_CREATED, saved);

        return saved;
    }

    public Announcement updateScheduledAnnouncement(Long adminUserId,
                                                    Long announcementId,
                                                    String title,
                                                    String content,
                                                    LocalDateTime scheduledAt,
                                                    Boolean publishNow,
                                                    Boolean sendNotification,
                                                    String pushTitle,
                                                    String pushBody,
                                                    String inAppBody,
                                                    List<AdminAnnouncementImageRequest> images) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 공지사항이 존재하지 않아요."));

        if (announcement.getStatus() == AnnouncementStatus.PUBLISHED) {
            throw new BadRequestException("이미 게시된 공지사항은 수정할 수 없어요.");
        }

        if (!Boolean.TRUE.equals(publishNow) && scheduledAt == null && announcement.getScheduledAt() == null) {
            throw new BadRequestException("게시 예정 시각을 입력하거나 즉시 게시 옵션을 선택해 주세요.");
        }
        validateNotificationOptions(sendNotification, pushTitle, pushBody, inAppBody);

        OffsetDateTime now = publicationService.nowKst();
        OffsetDateTime newSchedule = publicationService.toKstOffset(scheduledAt);

        List<String> previousKeys = announcement.getImages().stream()
                .map(AnnouncementImage::getObjectKey)
                .toList();

        announcement.updateContent(title, content);
        announcement.updateNotificationOptions(sendNotification, pushTitle, pushBody, inAppBody);

        if (Boolean.TRUE.equals(publishNow)) {
            publicationService.publishAnnouncement(announcement, now);
        } else if (newSchedule != null) {
            announcement.reschedule(newSchedule);
            if (!newSchedule.isAfter(now)) {
                publicationService.publishAnnouncement(announcement, newSchedule);
            }
        }

        announcement.replaceImages(toImages(images));

        cleanupRemovedImages(previousKeys, announcement.getImages());

        User admin = loadAdmin(adminUserId);
        recordAudit(admin, AdminAuditAction.ANNOUNCEMENT_UPDATED, announcement);

        return announcement;
    }

    public void deleteAnnouncement(Long adminUserId, Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 공지사항이 존재하지 않아요."));

        List<String> imageKeys = announcement.getImages().stream()
                .map(AnnouncementImage::getObjectKey)
                .filter(Objects::nonNull)
                .toList();

        User admin = loadAdmin(adminUserId);
        recordAudit(admin, AdminAuditAction.ANNOUNCEMENT_DELETED, announcement);

        announcementRepository.delete(announcement);

        if (!imageKeys.isEmpty()) {
            runAfterCommitOrNow(() -> imageService.deleteAll(imageVariantService.associatedKeys(ImageKind.ANNOUNCEMENT_IMAGE, imageKeys)));
        }
    }

    public List<Announcement> listAnnouncements() {
        return announcementRepository.findAllOrderByLatest();
    }

    public Announcement getAnnouncement(Long announcementId) {
        return announcementRepository.findById(announcementId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 공지사항이 존재하지 않아요."));
    }

    public AnnouncementImagePresignResponse generateImagePresignUrl(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new BadRequestException("contentType 누락입니다.");
        }

        String fileName = UUID.randomUUID().toString();
        String objectKey = "announcements/" + fileName;
        String uploadUrl = imageService.generateUploadUrl(objectKey, contentType, 10);

        String imageUrl = imageDomainService.toUploadImageUrl(objectKey);

        return new AnnouncementImagePresignResponse(uploadUrl, objectKey, imageUrl);
    }

    private void validateScheduleRequest(LocalDateTime scheduledAt, Boolean publishNow) {
        if (!Boolean.TRUE.equals(publishNow) && scheduledAt == null) {
            throw new BadRequestException("게시 예정 시각을 입력하거나 즉시 게시 옵션을 선택해 주세요.");
        }
    }

    private void validateNotificationOptions(Boolean sendNotification,
                                             String pushTitle,
                                             String pushBody,
                                             String inAppBody) {
        if (!Boolean.TRUE.equals(sendNotification)) {
            return;
        }
        if (pushTitle == null || pushTitle.isBlank()
                || pushBody == null || pushBody.isBlank()
                || inAppBody == null || inAppBody.isBlank()) {
            throw new BadRequestException("알림을 보낼 경우 푸시 제목/본문과 인앱 알림 본문을 모두 입력해 주세요.");
        }
    }

    private User loadAdmin(Long adminUserId) {
        return userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 정보를 찾을 수 없어요."));
    }

    private List<AnnouncementImage> toImages(List<AdminAnnouncementImageRequest> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
                .filter(Objects::nonNull)
                .filter(i -> i.objectKey() != null && !i.objectKey().isBlank())
                .map(i -> AnnouncementImage.of(i.objectKey(), i.contentType()))
                .toList();
    }

    private void cleanupRemovedImages(List<String> previousKeys, List<AnnouncementImage> newImages) {
        if (previousKeys == null || previousKeys.isEmpty()) {
            return;
        }

        Set<String> currentKeys = newImages == null ? Set.of() : newImages.stream()
                .map(AnnouncementImage::getObjectKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<String> removed = previousKeys.stream()
                .filter(Objects::nonNull)
                .filter(k -> !currentKeys.contains(k))
                .toList();

        if (removed.isEmpty()) {
            return;
        }

        runAfterCommitOrNow(() -> imageService.deleteAll(imageVariantService.associatedKeys(ImageKind.ANNOUNCEMENT_IMAGE, removed)));
    }

    private void recordAudit(User admin, AdminAuditAction action, Announcement announcement) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("title", announcement.getTitle());
        metadata.put("status", announcement.getStatus());
        metadata.put("scheduledAt", announcement.getScheduledAt());
        metadata.put("publishedAt", announcement.getPublishedAt());

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                action,
                AdminAuditTargetType.ANNOUNCEMENT,
                announcement.getId(),
                null,
                metadata
        ));
    }
}
