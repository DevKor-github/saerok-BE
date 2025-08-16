package org.devkor.apu.saerok_server.domain.notification.application.gateway;

import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;

public interface PushGateway {
    void sendToUser(Long userId, NotificationSubject subject, NotificationAction action, PushMessageCommand cmd);
}
