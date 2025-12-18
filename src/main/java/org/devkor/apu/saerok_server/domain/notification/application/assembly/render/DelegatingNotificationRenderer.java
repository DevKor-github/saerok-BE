package org.devkor.apu.saerok_server.domain.notification.application.assembly.render;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.SystemNotificationPayload;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
public class DelegatingNotificationRenderer implements NotificationRenderer {

    private final ActionNotificationRenderer actionRenderer;
    private final SystemNotificationRenderer systemRenderer;

    @Override
    public RenderedMessage render(NotificationPayload payload) {
        if (payload instanceof ActionNotificationPayload) {
            return actionRenderer.render(payload);
        }
        if (payload instanceof SystemNotificationPayload) {
            return systemRenderer.render(payload);
        }
        throw new IllegalArgumentException("Unsupported payload: " + payload.getClass());
    }
}
