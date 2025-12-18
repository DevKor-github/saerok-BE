package org.devkor.apu.saerok_server.domain.notification.application.assembly.render;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.SystemNotificationPayload;
import org.devkor.apu.saerok_server.global.core.config.feature.NotificationMessagesConfig;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SystemNotificationRenderer implements NotificationRenderer {

    private final NotificationMessagesConfig messages;

    @Override
    public RenderedMessage render(NotificationPayload p) {
        if (!(p instanceof SystemNotificationPayload s)) {
            throw new IllegalArgumentException("Unsupported payload: " + p.getClass());
        }

        NotificationMessagesConfig.Template t = messages.forType(s.type());

        var vars = NotificationTemplateRenderer.toVars(s.extras());

        return NotificationTemplateRenderer.render(t, vars);
    }
}
