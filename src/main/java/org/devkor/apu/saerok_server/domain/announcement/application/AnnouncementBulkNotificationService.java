package org.devkor.apu.saerok_server.domain.announcement.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.entity.Announcement;
import org.devkor.apu.saerok_server.domain.admin.announcement.core.repository.AnnouncementRepository;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.SystemNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.Notification;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.devkor.apu.saerok_server.domain.notification.infra.fcm.FcmMessageClient;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementBulkNotificationService {

    private static final int CHUNK_SIZE = 100;
    private static final int BATCH_INSERT_SIZE = 100;
    private static final int FCM_MAX_TOKENS = 500;

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final NotificationSettingRepository settingRepository;
    private final NotificationRenderer renderer;
    private final FcmMessageClient fcmMessageClient;

    @Transactional
    public void sendBulkNotification(Long announcementId) {
        Announcement ann = announcementRepository.findById(announcementId).orElse(null);
        if (ann == null || !ann.isPublished() || !Boolean.TRUE.equals(ann.getSendNotification())) {
            return;
        }

        Long annId = ann.getId();
        NotificationType type = NotificationType.SYSTEM_PUBLISHED_ANNOUNCEMENT;
        Map<String, Object> extras = Map.of(
                "announcementId", annId,
                "title", ann.getPushTitle(),
                "body", ann.getPushBody(),
                "inAppBody", ann.getInAppBody()
        );

        SystemNotificationPayload templatePayload = new SystemNotificationPayload(0L, type, annId, extras);
        NotificationRenderer.RenderedMessage rendered = renderer.render(templatePayload);

        int offset = 0;
        while (true) {
            List<Long> userIds = userRepository.findActiveUserIds(offset, CHUNK_SIZE);
            if (userIds.isEmpty()) break;

            processChunk(userIds, type, extras, rendered, annId);
            offset += userIds.size();

            if (userIds.size() < CHUNK_SIZE) break;
        }

        log.info("Bulk announcement notification completed: announcementId={}, totalUsers={}",
                annId, offset);
    }

    private void processChunk(List<Long> userIds, NotificationType type,
                              Map<String, Object> extras,
                              NotificationRenderer.RenderedMessage rendered,
                              Long announcementId) {
        // (a) 유저 배치 조회
        List<User> users = userRepository.findByIds(userIds);

        // (b) 인앱 알림 batch insert
        List<Notification> notifications = users.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .type(type)
                        .isRead(false)
                        .payload(new HashMap<>(extras))
                        .build())
                .toList();
        notificationRepository.batchInsert(notifications, BATCH_INSERT_SIZE);

        // (c) 활성 디바이스 조회 (1 쿼리)
        List<Long> enabledDeviceIds = settingRepository
                .findEnabledDeviceIdsByUserIdsAndType(userIds, type);
        if (enabledDeviceIds.isEmpty()) return;

        // (d) 토큰 조회 (1 쿼리)
        List<String> tokens = userDeviceRepository.findTokensByUserDeviceIds(enabledDeviceIds);
        if (tokens.isEmpty()) return;

        // (e) FCM 전송
        PushMessageCommand cmd = PushMessageCommand.createPushMessageCommand(
                rendered.pushTitle(), rendered.pushBody(),
                type.name(), announcementId, 1, null);

        for (int i = 0; i < tokens.size(); i += FCM_MAX_TOKENS) {
            List<String> batch = tokens.subList(i, Math.min(i + FCM_MAX_TOKENS, tokens.size()));
            fcmMessageClient.sendToDevices(batch, cmd);
        }
    }
}
