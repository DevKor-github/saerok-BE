package org.devkor.apu.saerok_server.domain.notification.application.facade;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.deeplink.DeepLinkResolver;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.render.NotificationRenderer.RenderedMessage;
import org.devkor.apu.saerok_server.domain.notification.application.assembly.store.InAppNotificationWriter;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.application.gateway.PushGateway;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
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
    public void push(NotificationPayload payload, org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target target) {
        RenderedMessage r = renderer.render(payload);
        String deepLink = deepLinkResolver.resolve(target);
        inAppWriter.save(payload, r, deepLink);

        int unread = notificationRepository.countUnreadByUserId(payload.recipientId()).intValue();

        if (!(payload instanceof ActionNotificationPayload a)) {
            throw new IllegalArgumentException("Unsupported payload: " + payload.getClass());
        }

        // notificationType 자리에 action.name()을 내려 푸시 data에 담기도록 함
        PushMessageCommand cmd = PushMessageCommand.createPushMessageCommand(
                r.pushTitle(), r.pushBody(), a.action().name(), a.relatedId(), deepLink, unread
        );

        // subject/action 축으로 게이트웨이에 전달
        pushGateway.sendToUser(a.recipientId(), a.subject(), a.action(), cmd);
    }
}
