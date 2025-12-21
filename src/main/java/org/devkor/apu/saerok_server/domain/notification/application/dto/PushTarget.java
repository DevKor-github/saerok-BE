package org.devkor.apu.saerok_server.domain.notification.application.dto;

import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

public record PushTarget(
        Long userId,
        NotificationType type,
        PushMessageCommand command
) {
}
