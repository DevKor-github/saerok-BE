package org.devkor.apu.saerok_server.domain.notification.application.assembly.store;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.batch.BatchActor;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.BatchedNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
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
        Map<String, Object> payloadMap = new HashMap<>();

        // 공통 extras 복사
        if (payload.extras() != null) {
            payloadMap.putAll(payload.extras());
        }

        if (payload instanceof ActionNotificationPayload a) {
            actor = userRepository.findById(a.actorId())
                    .orElseThrow(() -> new IllegalArgumentException("Actor not found: " + a.actorId()));
        }

        if (payload instanceof BatchedNotificationPayload b) {
            // 첫 번째 액터를 대표 액터로 저장
            BatchActor firstActor = b.getFirstActor();
            actor = userRepository.findById(firstActor.id())
                    .orElse(null); // 배치 알림의 경우 액터가 없을 수도 있음 (삭제된 사용자)

            payloadMap.put("actorCount", b.actorCount());
            payloadMap.put("actors", b.actors().stream()
                    .map(a -> Map.of("id", a.id(), "name", a.name()))
                    .toList());
        }

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
