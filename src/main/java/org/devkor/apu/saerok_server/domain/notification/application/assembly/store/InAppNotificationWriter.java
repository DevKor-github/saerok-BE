package org.devkor.apu.saerok_server.domain.notification.application.assembly.store;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.Notification;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationRepository;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationTypeResolver;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class InAppNotificationWriter {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void save(NotificationPayload payload, String deepLink) {
        if (!(payload instanceof ActionNotificationPayload a)) {
            throw new IllegalArgumentException("Unsupported payload: " + payload.getClass());
        }

        User recipient = userRepository.findById(a.recipientId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found: " + a.recipientId()));

        User actor = userRepository.findById(a.actorId())
                .orElseThrow(() -> new IllegalArgumentException("Actor not found: " + a.actorId()));

        NotificationType type = NotificationTypeResolver.from(a.subject(), a.action());

        // extras + relatedId를 합쳐 payload(jsonb)에 저장
        Map<String, Object> payloadMap = new HashMap<>();
        if (a.extras() != null) payloadMap.putAll(a.extras());
        payloadMap.put("relatedId", a.relatedId());

        Notification entity = Notification.builder()
                .user(recipient)
                .type(type)
                // .relatedId(a.relatedId())  // 더 이상 직접 쓰지 않음 (payload.relatedId로 이동)
                .deepLink(deepLink)
                .actor(actor)
                .isRead(false)
                .payload(payloadMap)
                .build();

        notificationRepository.save(entity);
    }
}
