package org.devkor.apu.saerok_server.domain.notification.application.assembly.store;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.SystemNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.Notification;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationRepository;
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

    public Long save(NotificationPayload payload) {
        User recipient = userRepository.findById(payload.recipientId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found: " + payload.recipientId()));

        NotificationType type = payload.type();

        User actor = null;
        if (payload instanceof ActionNotificationPayload a) {
            actor = userRepository.findById(a.actorId())
                    .orElseThrow(() -> new IllegalArgumentException("Actor not found: " + a.actorId()));
        }

        Map<String, Object> payloadMap = new HashMap<>();
        if (payload.extras() != null) payloadMap.putAll(payload.extras());

        Notification entity = Notification.builder()
                .user(recipient)
                .type(type)
                .actor(actor)
                .isRead(false)
                .payload(payloadMap)
                .build();

        notificationRepository.save(entity);
        notificationRepository.flush();
        return entity.getId();
    }
}
