package org.devkor.apu.saerok_server.domain.notification.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.dto.*;
import org.devkor.apu.saerok_server.domain.notification.core.entity.DeviceToken;
import org.devkor.apu.saerok_server.domain.notification.core.repository.DeviceTokenRepository;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PushNotificationService {

    private final FcmMessageService fcmMessageService;
    private final DeviceTokenRepository deviceTokenRepository;

    @Async("pushNotificationExecutor")
    public void sendToUser(SendPushToUserCommand command) {
        List<DeviceToken> deviceTokens = deviceTokenRepository
                .findActiveTokensByUserId(command.userId());

        if (deviceTokens.isEmpty()) {
            return;
        }

        List<String> fcmTokens = deviceTokens.stream()
                .map(DeviceToken::getToken)
                .toList();

        fcmMessageService.sendToDevices(fcmTokens, command.message());
    }

    @Async("pushNotificationExecutor")
    public void sendBroadcast(SendBroadcastPushCommand command) {
        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllActiveTokens();
        
        if (deviceTokens.isEmpty()) {
            return;
        }
        
        List<String> fcmTokens = deviceTokens.stream()
                .map(DeviceToken::getToken)
                .toList();
        
        fcmMessageService.sendToDevices(fcmTokens, command.message());
    }

    public void sendCollectionLikeNotification(Long targetUserId, String likerNickname, Long collectionId) {
        Map<String, String> data = Map.of(
                "collectionId", collectionId.toString(),
                "userId", targetUserId.toString()
        );
        
        PushMessageCommand message = PushMessageCommand.createWithData(
                "새로운 좋아요를 받았어요!",
                likerNickname + "님이 회원님의 컬렉션에 좋아요를 남겼어요.",
                "COLLECTION_LIKE",
                data
        );

        SendPushToUserCommand command = new SendPushToUserCommand(targetUserId, message);
        sendToUser(command);
    }

    public void sendCollectionCommentNotification(Long targetUserId, String commenterNickname, Long collectionId) {
        Map<String, String> data = Map.of(
                "collectionId", collectionId.toString(),
                "userId", targetUserId.toString()
        );
        
        PushMessageCommand message = PushMessageCommand.createWithData(
                "새로운 댓글이에요!",
                commenterNickname + "님이 회원님의 컬렉션에 댓글을 남겼어요.",
                "COLLECTION_COMMENT",
                data
        );

        SendPushToUserCommand command = new SendPushToUserCommand(targetUserId, message);
        sendToUser(command);
    }

    public void sendBirdIdSuggestionNotification(Long targetUserId, String suggesterNickname, Long collectionId, String birdName) {
        Map<String, String> data = Map.of(
                "collectionId", collectionId.toString(),
                "userId", targetUserId.toString(),
                "birdName", birdName
        );
        
        PushMessageCommand message = PushMessageCommand.createWithData(
                "새로운 동정 의견이에요!",
                suggesterNickname + "님이 '" + birdName + "'로 제안했어요.",
                "BIRD_ID_SUGGESTION",
                data
        );

        SendPushToUserCommand command = new SendPushToUserCommand(targetUserId, message);
        sendToUser(command);
    }


}
