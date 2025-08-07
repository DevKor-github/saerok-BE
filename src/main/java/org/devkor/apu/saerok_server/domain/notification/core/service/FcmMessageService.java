package org.devkor.apu.saerok_server.domain.notification.core.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.core.repository.DeviceTokenRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmMessageService {

    private final FirebaseMessaging firebaseMessaging;
    private final DeviceTokenRepository deviceTokenRepository;

    @Async("pushNotificationExecutor")
    public void sendToDevices(List<String> fcmTokens, PushMessageCommand messageCommand) {
        if (fcmTokens.isEmpty()) {
            return;
        }

        MulticastMessage message = buildMulticastMessage(fcmTokens, messageCommand);
        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            
            // 실패한 토큰들 처리
            if (response.getFailureCount() > 0) {
                log.info("FCM 전송 일부 실패 - 전체: {}, 실패: {}", response.getResponses().size(), response.getFailureCount());
                handleFailedTokens(fcmTokens, response);
            } else {
                log.debug("FCM 전송 성공 - {} 개 토큰", fcmTokens.size());
            }
        } catch (FirebaseMessagingException e) {
            log.error("FCM 서비스 전체 오류 - 토큰 수: {}, 오류: {}", fcmTokens.size(), e.getMessage(), e);
        }
    }
    
    // 실패한 토큰들 처리
    private void handleFailedTokens(List<String> originalTokens, BatchResponse response) {
        List<String> invalidTokens = new ArrayList<>();
        
        for (int i = 0; i < response.getResponses().size(); i++) {
            SendResponse sendResponse = response.getResponses().get(i);
            if (!sendResponse.isSuccessful()) {
                FirebaseMessagingException exception = sendResponse.getException();
                if (exception != null && isInvalidToken(exception)) {
                    invalidTokens.add(originalTokens.get(i));
                }
            }
        }

        if (!invalidTokens.isEmpty()) {
            deviceTokenRepository.deleteByTokens(invalidTokens);
            log.info("무효한 FCM 토큰 {} 개 삭제 완료", invalidTokens.size());
        }
    }
    
    // 토큰이 무효한지 확인
    private boolean isInvalidToken(FirebaseMessagingException exception) {
        MessagingErrorCode errorCode = exception.getMessagingErrorCode();
        return errorCode == MessagingErrorCode.INVALID_ARGUMENT ||
               errorCode == MessagingErrorCode.UNREGISTERED;
    }

    // 멀티캐스트 FCM 메시지 생성
    private MulticastMessage buildMulticastMessage(List<String> fcmTokens, PushMessageCommand messageCommand) {
        MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                .addAllTokens(fcmTokens)
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
            messageCommand.data().forEach((key, value) -> {
                if ("relatedId".equals(key) || "deeplink".equals(key) || "birdName".equals(key)) {
                    data.put(key, value);
                }
            });
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
