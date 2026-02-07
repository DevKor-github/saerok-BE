package org.devkor.apu.saerok_server.domain.notification.infra.fcm;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmMessageClient {

    private final FirebaseMessaging firebaseMessaging;
    private final UserDeviceRepository userDeviceRepository;

    @Async("pushNotificationExecutor")
    public void sendToDevices(List<String> fcmTokens, PushMessageCommand cmd) {
        if (fcmTokens == null || fcmTokens.isEmpty()) return;

        MulticastMessage message = buildMulticast(fcmTokens, cmd);

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            cleanupInvalidTokens(fcmTokens, response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast send failed: code={}, msg={}", e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected FCM error: {}", e.getMessage(), e);
        }
    }

    /**
     * FCM HTTP v1 API는 apns/android 설정을 모두 포함하면 토큰 타입에 따라 자동 분기됨
     */
    private MulticastMessage buildMulticast(List<String> tokens, PushMessageCommand cmd) {
        Notification notification = Notification.builder()
                .setTitle(cmd.title())
                .setBody(cmd.body())
                .build();

        Map<String,String> data = new HashMap<>();
        if (cmd.notificationType() != null) data.put("type", cmd.notificationType());
        if (cmd.relatedId() != null)       data.put("relatedId", cmd.relatedId().toString());
        if (cmd.notificationId() != null)  data.put("notificationId", cmd.notificationId().toString());
        data.put("unreadCount", String.valueOf(cmd.unreadCount()));

        int badge = Math.max(0, Math.min(cmd.unreadCount(), 999));

        // iOS (APNs) 설정
        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setBadge(badge)
                        .setSound("default")
                        .build())
                .build();

        // Android 설정
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setSound("default")
                        .setDefaultSound(true)
                        .setPriority(AndroidNotification.Priority.HIGH)
                        .build())
                .build();

        MulticastMessage.Builder b = MulticastMessage.builder()
                .setNotification(notification)
                .setApnsConfig(apnsConfig)
                .setAndroidConfig(androidConfig);
        if (!data.isEmpty()) b.putAllData(data);
        tokens.forEach(b::addToken);
        return b.build();
    }

    @Async("pushNotificationExecutor")
    public void sendSilentBadgeUpdate(List<String> fcmTokens, int unreadCount) {
        if (fcmTokens == null || fcmTokens.isEmpty()) return;

        MulticastMessage message = buildSilentBadgeMessage(fcmTokens, unreadCount);

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            cleanupInvalidTokens(fcmTokens, response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM silent badge update failed: code={}, msg={}", e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected FCM silent badge update error: {}", e.getMessage(), e);
        }
    }

    private MulticastMessage buildSilentBadgeMessage(List<String> tokens, int unreadCount) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "UPDATE_BADGE");
        data.put("silent", "true");
        data.put("unreadCount", String.valueOf(unreadCount));

        int badge = Math.max(0, Math.min(unreadCount, 999));

        // iOS silent push: content-available으로 백그라운드 처리
        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setBadge(badge)
                        .setContentAvailable(true)
                        .build())
                .build();

        // Android data-only: priority normal (Doze 모드 정책 준수)
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.NORMAL)
                .build();

        MulticastMessage.Builder builder = MulticastMessage.builder()
                .putAllData(data)
                .setApnsConfig(apnsConfig)
                .setAndroidConfig(androidConfig);
        tokens.forEach(builder::addToken);
        return builder.build();
    }

    private List<String> collectInvalidTokens(List<String> tokens, BatchResponse resp) {
        List<String> invalid = new ArrayList<>();
        if (resp == null) return invalid;

        for (int i = 0; i < resp.getResponses().size(); i++) {
            SendResponse r = resp.getResponses().get(i);
            if (!r.isSuccessful() && r.getException() instanceof FirebaseMessagingException fme) {
                var code = fme.getMessagingErrorCode();
                if (code == MessagingErrorCode.INVALID_ARGUMENT || code == MessagingErrorCode.UNREGISTERED) {
                    invalid.add(tokens.get(i));
                }
            }
        }
        return invalid;
    }

    private void cleanupInvalidTokens(List<String> fcmTokens, BatchResponse response) {
        List<String> invalid = collectInvalidTokens(fcmTokens, response);

        if (!invalid.isEmpty()) {
            try {
                invalid.forEach(userDeviceRepository::deleteByToken);
            } catch (Exception e) {
                log.warn("Invalid FCM tokens cleanup failed ({} tokens): {}", invalid.size(), e.getMessage());
            }
        }
    }
}
