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

            List<String> invalid = collectInvalidTokens(fcmTokens, response);

            if (!invalid.isEmpty()) {
                try {
                    invalid.forEach(userDeviceRepository::deleteByToken);
                } catch (Exception e) {
                    log.warn("Invalid FCM tokens cleanup failed ({} tokens): {}", invalid.size(), e.getMessage());
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast send failed: code={}, msg={}", e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected FCM error: {}", e.getMessage(), e);
        }
    }

    private MulticastMessage buildMulticast(List<String> tokens, PushMessageCommand cmd) {
        boolean isBadgeRefresh = "BADGE_REFRESH".equals(cmd.notificationType());

        Map<String,String> data = new HashMap<>();
        if (cmd.notificationType() != null) data.put("type", cmd.notificationType());
        if (cmd.relatedId() != null)       data.put("relatedId", cmd.relatedId().toString());
        if (cmd.deepLink() != null)        data.put("deeplink", cmd.deepLink());
        data.put("unreadCount", String.valueOf(cmd.unreadCount()));

        int badge = Math.max(0, Math.min(cmd.unreadCount(), 999));
        Aps.Builder apsBuilder = Aps.builder()
                .setBadge(badge);

        if (isBadgeRefresh) {
            apsBuilder.setContentAvailable(true);
        } else {
            apsBuilder.setSound("default");
        }
        ApnsConfig apns = ApnsConfig.builder()
                .setAps(apsBuilder.build())
                .build();

        Notification notification = null;
        if (!isBadgeRefresh) {
            notification = Notification.builder()
                    .setTitle(cmd.title())
                    .setBody(cmd.body())
                    .build();
        }

        MulticastMessage.Builder b = MulticastMessage.builder()
                .setApnsConfig(apns)
                .putAllData(data);

        if (notification != null) {
            b.setNotification(notification);
        }

        tokens.forEach(b::addToken);
        return b.build();
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
}
