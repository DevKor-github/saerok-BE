package org.devkor.apu.saerok_server.domain.notification.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.*;
import org.devkor.apu.saerok_server.domain.notification.core.repository.DeviceTokenRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingsRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final FcmMessageService fcmMessageService;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Async("pushNotificationExecutor")
    @Transactional
    public void sendToUser(Long userId, NotificationType notificationType, PushMessageCommand message) {
        List<NotificationSettings> settings = notificationSettingsRepository
                .findByUserIdWithNotificationEnabled(userId, notificationType);

        if (settings.isEmpty()) {
            return;
        }

        List<String> expectedDeviceIds = settings.stream()
                .map(NotificationSettings::getDeviceId)
                .toList();

        List<DeviceToken> foundDeviceTokens = deviceTokenRepository.findByUserIdAndDeviceIds(userId, expectedDeviceIds);

        // 데이터 정합성 체크 및 로깅
        if (foundDeviceTokens.size() != expectedDeviceIds.size()) {
            List<String> foundDeviceIds = foundDeviceTokens.stream()
                    .map(DeviceToken::getDeviceId)
                    .toList();
            List<String> missingDeviceIds = expectedDeviceIds.stream()
                    .filter(id -> !foundDeviceIds.contains(id))
                    .toList();
            log.warn("데이터 불일치 발견: userId={}의 deviceId={}에 대한 DeviceToken이 존재하지 않습니다.", userId, missingDeviceIds);
        }

        if (foundDeviceTokens.isEmpty()) {
            return;
        }

        List<String> fcmTokens = foundDeviceTokens.stream()
                .map(DeviceToken::getToken)
                .toList();

        fcmMessageService.sendToDevices(fcmTokens, message);
    }

    @Transactional
    public void sendCollectionLikeNotification(Long targetUserId, Long likerUserId, Long collectionId) {
        User targetUser = userRepository.findById(targetUserId).orElse(null);
        User likerUser = userRepository.findById(likerUserId).orElse(null);
        
        if (targetUser == null || likerUser == null) {
            log.warn("사용자를 찾을 수 없음: targetUserId={}, likerUserId={}", targetUserId, likerUserId);
            return;
        }

        if (notificationSettingsRepository.findByUserIdWithNotificationEnabled(targetUserId, NotificationType.LIKE).isEmpty()) {
            return;
        }

        String deepLink = "saerok://collection/" + collectionId;

        // 1. 알림 목록용 내용 (Notification 엔티티에 저장)
        String inAppTitle = likerUser.getNickname();
        String inAppBody = "나의 새록을 좋아해요.";

        Notification notification = Notification.builder()
                .user(targetUser)
                .title(inAppTitle)
                .body(inAppBody)
                .type(NotificationType.LIKE)
                .relatedId(collectionId)
                .deepLink(deepLink)
                .sender(likerUser)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);

        // 2. FCM 푸시용 내용 (실제 기기로 전송)
        String pushTitle = likerUser.getNickname() + "님이 좋아요를 눌렀어요!";
        String pushBody = "나의 새록을 좋아해요";

        Map<String, String> data = Map.of(
                "type", "LIKE",
                "relatedId", collectionId.toString(),
                "deeplink", deepLink
        );
        
        PushMessageCommand message = PushMessageCommand.createWithDataAndDeepLink(
                pushTitle, pushBody, "LIKE", data, deepLink);

        sendToUser(targetUserId, NotificationType.LIKE, message);
    }

    @Transactional
    public void sendCollectionCommentNotification(Long targetUserId, Long commenterUserId, Long collectionId, String commentContent) {
        User targetUser = userRepository.findById(targetUserId).orElse(null);
        User commenterUser = userRepository.findById(commenterUserId).orElse(null);
        
        if (targetUser == null || commenterUser == null) {
            log.warn("사용자를 찾을 수 없음: targetUserId={}, commenterUserId={}", targetUserId, commenterUserId);
            return;
        }

        if (notificationSettingsRepository.findByUserIdWithNotificationEnabled(targetUserId, NotificationType.COMMENT).isEmpty()) {
            return;
        }

        String deepLink = "saerok://collection/" + collectionId;

        // 1. 알림 목록용 내용 (Notification 엔티티에 저장)
        String inAppTitle = commenterUser.getNickname();
        String inAppBody = "나의 새록에 댓글을 남겼어요. \"" + commentContent + "\"";

        Notification notification = Notification.builder()
                .user(targetUser)
                .title(inAppTitle)
                .body(inAppBody)
                .type(NotificationType.COMMENT)
                .relatedId(collectionId)
                .deepLink(deepLink)
                .sender(commenterUser)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);

        // 2. FCM 푸시용 내용 (실제 기기로 전송)
        String pushTitle = commenterUser.getNickname() + "님이 댓글을 남겼어요!";
        String pushBody = "나의 새록에 댓글을 남겼어요. \"" + commentContent + "\"";

        Map<String, String> data = Map.of(
                "type", "COMMENT",
                "relatedId", collectionId.toString(),
                "deeplink", deepLink
        );

        PushMessageCommand message = PushMessageCommand.createWithDataAndDeepLink(
                pushTitle, pushBody, "COMMENT", data, deepLink);

        sendToUser(targetUserId, NotificationType.COMMENT, message);
    }

    @Transactional
    public void sendBirdIdSuggestionNotification(Long targetUserId, Long suggesterUserId, Long collectionId, String birdName) {
        User targetUser = userRepository.findById(targetUserId).orElse(null);
        User suggesterUser = userRepository.findById(suggesterUserId).orElse(null);
        
        if (targetUser == null || suggesterUser == null) {
            log.warn("사용자를 찾을 수 없음: targetUserId={}, suggesterUserId={}", targetUserId, suggesterUserId);
            return;
        }

        if (notificationSettingsRepository.findByUserIdWithNotificationEnabled(targetUserId, NotificationType.BIRD_ID_SUGGESTION).isEmpty()) {
            return;
        }

        String deepLink = "saerok://collection/" + collectionId;

        // 1. 알림 목록용 내용 (Notification 엔티티에 저장)
        String inAppTitle = "동정 의견 공유";
        String inAppBody = "두근두근! 새로운 의견이 공유됐어요. 확인해볼까요?";

        Notification notification = Notification.builder()
                .user(targetUser)
                .title(inAppTitle)
                .body(inAppBody)
                .type(NotificationType.BIRD_ID_SUGGESTION)
                .relatedId(collectionId)
                .deepLink(deepLink)
                .sender(suggesterUser)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);

        // 2. FCM 푸시용 내용 (실제 기기로 전송) - 동일한 내용 사용
        String pushTitle = "동정 의견 공유";
        String pushBody = "두근두근! 새로운 의견이 공유됐어요. 확인해볼까요?";

        Map<String, String> data = Map.of(
                "type", "BIRD_ID_SUGGESTION",
                "relatedId", collectionId.toString(),
                "deeplink", deepLink,
                "birdName", birdName
        );
        
        PushMessageCommand message = PushMessageCommand.createWithDataAndDeepLink(
                pushTitle, pushBody, "BIRD_ID_SUGGESTION", data, deepLink);

        sendToUser(targetUserId, NotificationType.BIRD_ID_SUGGESTION, message);
    }
}
