package org.devkor.apu.saerok_server.domain.notification.application.assembly.render;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.global.core.config.feature.NotificationMessagesConfig;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActionNotificationRenderer implements NotificationRenderer {

    private final NotificationMessagesConfig messages;

    @Override
    public RenderedMessage render(NotificationPayload p) {
        if (!(p instanceof ActionNotificationPayload a)) {
            throw new IllegalArgumentException("Unsupported payload: " + p.getClass());
        }

        NotificationMessagesConfig.Template t = messages.forType(a.type());

        var vars = NotificationTemplateRenderer.toVars(a.extras());
        vars.put("actorName", NotificationTemplateRenderer.nullToEmpty(a.actorName()));

        return NotificationTemplateRenderer.render(t, vars);
    }
}
