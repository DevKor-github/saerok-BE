package org.devkor.apu.saerok_server.domain.notification.application.assembly.store;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer.RenderedMessage;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.Notification;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationRepository;
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationTypeResolver;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InAppNotificationWriter {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void save(NotificationPayload payload, RenderedMessage r, String deepLink) {
        if (!(payload instanceof ActionNotificationPayload a)) {
            throw new IllegalArgumentException("Unsupported payload: " + payload.getClass());
        }

        User recipient = userRepository.findById(a.recipientId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found: " + a.recipientId()));

        User actor = userRepository.findById(a.actorId())
                .orElseThrow(() -> new IllegalArgumentException("Actor not found: " + a.actorId()));

        NotificationType type = NotificationTypeResolver.from(a.subject(), a.action());

        Notification entity = Notification.builder()
                .user(recipient)
                .body(r.inAppBody())
                .type(type)
                .relatedId(a.relatedId())
                .deepLink(deepLink)
                .actor(actor)
                .isRead(false)
                .build();

        notificationRepository.save(entity);
    }
}
