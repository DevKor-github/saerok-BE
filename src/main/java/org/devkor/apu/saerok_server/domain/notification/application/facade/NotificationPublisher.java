package org.devkor.apu.saerok_server.domain.notification.application.facade;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.store.InAppNotificationWriter;
import org.devkor.apu.saerok_server.domain.notification.application.gateway.PushGateway;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.deeplink.DeepLinkResolver;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer.RenderedMessage;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final NotificationRenderer renderer;
    private final InAppNotificationWriter inAppWriter;
    private final NotificationRepository notificationRepository;
    private final PushGateway pushGateway;
    private final DeepLinkResolver deepLinkResolver;

    @Transactional
    public void push(NotificationPayload payload, Target target) {
        // 1) 렌더링 (인앱/푸시 동시 생성)
        RenderedMessage r = renderer.render(payload);

        // 2) 딥링크
        String deepLink = deepLinkResolver.resolve(target);

        // 3) 인앱 저장 (body만)
        inAppWriter.save(payload, r, deepLink);

        // 4) 배지 카운트
        int unread = notificationRepository.countUnreadByUserId(payload.recipientId()).intValue();

        // 5) 푸시 전송 (title/body 분리 사용)
        PushMessageCommand cmd = PushMessageCommand.createPushMessageCommand(
                r.pushTitle(), r.pushBody(), payload.type().name(), payload.relatedId(), deepLink, unread
        );

        // 실제 디바이스/설정 필터링 + FCM 전송
        pushGateway.sendToUser(payload.recipientId(), payload.type(), cmd);
    }
}
