package org.devkor.apu.saerok_server.domain.notification.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.*;
import org.devkor.apu.saerok_server.domain.notification.core.repository.DeviceTokenRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingsRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PushNotificationService {

    private final FcmMessageService fcmMessageService;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Async("pushNotificationExecutor")
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

        if (foundDeviceTokens.isEmpty()) {
            return;
        }

        List<String> fcmTokens = foundDeviceTokens.stream()
                .map(DeviceToken::getToken)
                .toList();

        fcmMessageService.sendToDevices(fcmTokens, message);
    }

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
        
        // 인앱 알림 저장
        saveInAppNotification(targetUser, likerUser, NotificationType.LIKE, collectionId, deepLink,
                likerUser.getNickname(), "나의 새록을 좋아해요.");

        // 푸시 알림 전송
        String pushTitle = likerUser.getNickname() + "님이 좋아요를 눌렀어요!";
        String pushBody = "나의 새록을 좋아해요";
        
        PushMessageCommand message = createPushMessage(pushTitle, pushBody, "LIKE", 
                Map.of("relatedId", collectionId.toString()), deepLink);

        sendToUser(targetUserId, NotificationType.LIKE, message);
    }

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
        String inAppBody = "나의 새록에 댓글을 남겼어요. \"" + commentContent + "\"";
        
        // 인앱 알림 저장
        saveInAppNotification(targetUser, commenterUser, NotificationType.COMMENT, collectionId, deepLink,
                commenterUser.getNickname(), inAppBody);

        // 푸시 알림 전송
        String pushTitle = commenterUser.getNickname() + "님이 댓글을 남겼어요!";
        
        PushMessageCommand message = createPushMessage(pushTitle, inAppBody, "COMMENT", 
                Map.of("relatedId", collectionId.toString()), deepLink);

        sendToUser(targetUserId, NotificationType.COMMENT, message);
    }

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
        String title = "동정 의견 공유";
        String body = "두근두근! 새로운 의견이 공유됐어요. 확인해볼까요?";
        
        // 인앱 알림 저장
        saveInAppNotification(targetUser, suggesterUser, NotificationType.BIRD_ID_SUGGESTION, collectionId, deepLink, title, body);

        // 푸시 알림 전송
        Map<String, String> data = Map.of(
                "relatedId", collectionId.toString(),
                "birdName", birdName
        );
        
        PushMessageCommand message = createPushMessage(title, body, "BIRD_ID_SUGGESTION", data, deepLink);

        sendToUser(targetUserId, NotificationType.BIRD_ID_SUGGESTION, message);
    }

    // === Private Helper Methods ===

    // 인앱 알림을 저장합니다.
    private void saveInAppNotification(User targetUser, User sender, NotificationType type, 
                                       Long relatedId, String deepLink, String title, String body) {
        Notification notification = Notification.builder()
                .user(targetUser)
                .title(title)
                .body(body)
                .type(type)
                .relatedId(relatedId)
                .deepLink(deepLink)
                .sender(sender)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
    }

    // 푸시 메시지 커맨드를 생성합니다.
    private PushMessageCommand createPushMessage(String title, String body, String type, 
                                                  Map<String, String> data, String deepLink) {
        return PushMessageCommand.createWithDataAndDeepLink(title, body, type, data, deepLink);
    }
}
