package org.devkor.apu.saerok_server.domain.notification.application.assembly.store;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer.RenderedMessage;
import org.devkor.apu.saerok_server.domain.notification.core.entity.Notification;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationRepository;
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

        Notification entity = Notification.builder()
                .user(recipient)
                .body(r.inAppBody())
                .type(a.type())
                .relatedId(a.relatedId())
                .deepLink(deepLink)
                .sender(null)              // TODO: sender를 actor로 바꾸고, 반영
                .isRead(false)
                .build();

        notificationRepository.save(entity);
    }
}
