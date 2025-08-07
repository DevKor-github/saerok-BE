package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.dto.*;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DeviceToken;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSettings;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.repository.DeviceTokenRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final FcmMessageService fcmMessageService;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;

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

    public void sendCollectionLikeNotification(Long targetUserId, String likerNickname, Long collectionId) {
        if (notificationSettingsRepository.findByUserIdWithNotificationEnabled(targetUserId, NotificationType.LIKE).isEmpty()) {
            return;
        }

        Map<String, String> data = Map.of(
                "collectionId", collectionId.toString(),
                "userId", targetUserId.toString()
        );
        
        PushMessageCommand message = PushMessageCommand.createWithData(
                likerNickname,
                "나의 새록을 좋아해요",
                "COLLECTION_LIKE",
                data
        );

        sendToUser(targetUserId, NotificationType.LIKE, message);
    }

    public void sendCollectionCommentNotification(Long targetUserId, String commenterNickname, Long collectionId, String commentContent) {
        if (notificationSettingsRepository.findByUserIdWithNotificationEnabled(targetUserId, NotificationType.COMMENT).isEmpty()) {
            return;
        }

        Map<String, String> data = Map.of(
                "collectionId", collectionId.toString(),
                "userId", targetUserId.toString()
        );

        PushMessageCommand message = PushMessageCommand.createWithData(
                commenterNickname,
                "나의 새록에 댓글을 남겼어요. \"" + commentContent + "\"",
                "COLLECTION_COMMENT",
                data
        );

        sendToUser(targetUserId, NotificationType.COMMENT, message);
    }

    public void sendBirdIdSuggestionNotification(Long targetUserId, String suggesterNickname, Long collectionId, String birdName) {
        if (notificationSettingsRepository.findByUserIdWithNotificationEnabled(targetUserId, NotificationType.BIRD_ID_SUGGESTION).isEmpty()) {
            return;
        }

        Map<String, String> data = Map.of(
                "collectionId", collectionId.toString(),
                "userId", targetUserId.toString(),
                "birdName", birdName
        );
        
        PushMessageCommand message = PushMessageCommand.createWithData(
                "동정 의견 공유",
                "두근두근! 새로운 의견이 공유됐어요. 확인해볼까요?",
                "BIRD_ID_SUGGESTION",
                data
        );

        sendToUser(targetUserId, NotificationType.BIRD_ID_SUGGESTION, message);
    }
}
