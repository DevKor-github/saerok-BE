package org.devkor.apu.saerok_server.domain.notification.application.assembly.store;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer.RenderedMessage;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.Notification;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
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

        // TEMP: action -> legacy NotificationType 매핑 (추후 Notification 자체도 subject/action으로 교체 가능)
        NotificationType legacy = switch (a.action()) {
            case LIKE -> NotificationType.LIKE;
            case COMMENT -> NotificationType.COMMENT;
            case SUGGEST_BIRD_ID -> NotificationType.BIRD_ID_SUGGESTION;
        };

        Notification entity = Notification.builder()
                .user(recipient)
                .body(r.inAppBody())
                .type(legacy)            // ← 기존 필드 유지
                .relatedId(a.relatedId())
                .deepLink(deepLink)
                .sender(null)
                .isRead(false)
                .build();

        notificationRepository.save(entity);
    }
}
