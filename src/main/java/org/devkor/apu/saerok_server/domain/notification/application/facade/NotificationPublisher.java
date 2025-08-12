package org.devkor.apu.saerok_server.domain.notification.application.facade;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.store.InAppNotificationWriter;
import org.devkor.apu.saerok_server.domain.notification.application.gateway.PushDispatchService;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.deeplink.DeepLinkResolver;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer.RenderedNotification;
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
    private final PushDispatchService pushDispatchService;
    private final DeepLinkResolver deepLinkResolver;

    @Transactional
    public void push(NotificationPayload payload, Target target) {
        // 1) 렌더링
        RenderedNotification r = renderer.render(payload);

        // 2) 딥링크
        String deepLink = deepLinkResolver.resolve(target);

        // 3) 인앱 저장
        inAppWriter.save(payload, r, deepLink);

        // 4) 배지 카운트
        int unread = notificationRepository.countUnreadByUserId(payload.recipientId()).intValue();

        // 5) 푸시 전송
        PushMessageCommand cmd = PushMessageCommand.createPushMessageCommand(
                r.title(), r.body(), payload.type().name(), payload.relatedId(), deepLink, unread
        );

        // 실제 디바이스/설정 필터링 + FCM 전송
        pushDispatchService.sendToUser(payload.recipientId(), payload.type(), cmd);
    }
}
