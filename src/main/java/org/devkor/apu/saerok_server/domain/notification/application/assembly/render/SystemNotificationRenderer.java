package org.devkor.apu.saerok_server.domain.notification.application.assembly.render;

import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.SystemNotificationPayload;
import org.springframework.stereotype.Component;

@Component
public class SystemNotificationRenderer implements NotificationRenderer {

    @Override
    public RenderedMessage render(NotificationPayload p) {
        if (!(p instanceof SystemNotificationPayload s)) {
            throw new IllegalArgumentException("Unsupported payload: " + p.getClass());
        }

        String title = asString(s.extras().get("pushTitle"));
        String body  = asString(s.extras().get("pushBody"));

        return new RenderedMessage(nullToEmpty(title), nullToEmpty(body));
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
