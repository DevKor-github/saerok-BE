package org.devkor.apu.saerok_server.domain.notification.application.facade;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer.RenderedMessage;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.store.InAppNotificationWriter;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.application.gateway.PushGateway;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final NotificationRenderer renderer;
    private final InAppNotificationWriter inAppWriter;
    private final NotificationRepository notificationRepository;
    private final PushGateway pushGateway;
    private final UserRepository userRepository;

    /**
     * <h2>모든 알림의 공통 파이프라인</h2>
     *
     * <ol>
     *   <li>렌더링</li>
     *   <li>인앱 저장</li>
     *   <li>배지 카운트 계산</li>
     *   <li>푸시 발송</li>
     * </ol>
     */
    @Transactional
    public void push(NotificationPayload payload) {

        // recipient가 삭제/미존재면 조용히 무시
        if (userRepository.findById(payload.recipientId()).isEmpty()) {
            return;
        }

        RenderedMessage renderedMessage = renderer.render(payload);
        Long notificationId = inAppWriter.save(payload);

        int unread = notificationRepository.countUnreadByUserId(payload.recipientId()).intValue();

        PushMessageCommand cmd = PushMessageCommand.createPushMessageCommand(
                renderedMessage.pushTitle(),
                renderedMessage.pushBody(),
                payload.type().name(),
                payload.relatedId(),
                unread,
                notificationId
        );

        pushGateway.sendToUser(payload.recipientId(), payload.type(), cmd);
    }
}
