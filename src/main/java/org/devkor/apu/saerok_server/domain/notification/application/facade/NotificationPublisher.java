// ===== ./src/main/java/org/devkor/apu/saerok_server/domain/notification/application/facade/NotificationPublisher.java =====
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
import org.devkor.apu.saerok_server.domain.notification.core.service.NotificationTypeResolver;
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

        String typeString = NotificationTypeResolver.from(a.subject(), a.action()).name();

        PushMessageCommand cmd = PushMessageCommand.createPushMessageCommand(
                r.pushTitle(), r.pushBody(), typeString, a.relatedId(), deepLink, unread
        );

        // subject/action 축은 내부 용도이므로 게이트웨이에 그대로 전달 (게이트웨이 내부에서 type 기준 검사)
        pushGateway.sendToUser(a.recipientId(), a.subject(), a.action(), cmd);
    }
}
