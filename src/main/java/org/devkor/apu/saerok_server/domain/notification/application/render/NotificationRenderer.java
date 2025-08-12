package org.devkor.apu.saerok_server.domain.notification.application.render;

import org.devkor.apu.saerok_server.domain.notification.application.payload.NotificationPayload;

public interface NotificationRenderer {
    RenderedNotification render(NotificationPayload payload);

    record RenderedNotification(String title, String body) { }
}
