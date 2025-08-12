package org.devkor.apu.saerok_server.domain.notification.application.assembly.render;

import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;

public interface NotificationRenderer {
    RenderedNotification render(NotificationPayload payload);

    record RenderedNotification(String title, String body) { }
}
