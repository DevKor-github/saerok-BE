package org.devkor.apu.saerok_server.domain.notification.core.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmMessageService {

    private final FirebaseMessaging firebaseMessaging;
    private final FcmTokenCleanupService fcmTokenCleanupService;

    @Async("pushNotificationExecutor")
    public void sendToDevices(List<String> fcmTokens, PushMessageCommand messageCommand) {
        if (fcmTokens.isEmpty()) {
            return;
        }

        for (String fcmToken : fcmTokens) {
            try {
                Message message = buildMessage(fcmToken, messageCommand);
                firebaseMessaging.send(message);
            } catch (FirebaseMessagingException e) {
                log.warn("FCM 메시지 전송 실패 - 오류코드: {}, 메시지: {}", 
                        e.getErrorCode(), e.getMessage());
                
                if (fcmTokenCleanupService.isInvalidToken(e)) {
                    fcmTokenCleanupService.cleanupInvalidToken(fcmToken);
                }
            } catch (Exception e) {
                log.error("FCM 메시지 전송 중 예상치 못한 오류: {}", e.getMessage());
            }
        }
    }

    private Message buildMessage(String fcmToken, PushMessageCommand messageCommand) {
        Message.Builder messageBuilder = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(messageCommand.title())
                        .setBody(messageCommand.body())
                        .build());

        // data 필드 구성
        Map<String, String> data = new HashMap<>();
        if (messageCommand.notificationType() != null) {
            data.put("type", messageCommand.notificationType());
        }
        if (messageCommand.data() != null) {
            messageCommand.data().entrySet().stream()
                    .filter(entry -> "relatedId".equals(entry.getKey()) || "birdName".equals(entry.getKey()))
                    .forEach(entry -> data.put(entry.getKey(), entry.getValue()));
        }
        if (messageCommand.deepLink() != null) {
            data.put("deeplink", messageCommand.deepLink());
        }
        
        if (!data.isEmpty()) {
            messageBuilder.putAllData(data);
        }

        // iOS APNS 전용 설정
        messageBuilder.setApnsConfig(ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setAlert(ApsAlert.builder()
                                .setTitle(messageCommand.title())
                                .setBody(messageCommand.body())
                                .build())
                        .setBadge(1)
                        .setSound("default")
                        .build())
                .build());

        return messageBuilder.build();
    }
}
