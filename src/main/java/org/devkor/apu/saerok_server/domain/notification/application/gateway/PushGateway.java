package org.devkor.apu.saerok_server.domain.notification.application.gateway;

import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

public interface PushGateway {
    void sendToUser(Long userId, NotificationType type, PushMessageCommand cmd);
}
