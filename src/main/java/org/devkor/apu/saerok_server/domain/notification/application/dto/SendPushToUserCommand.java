package org.devkor.apu.saerok_server.domain.notification.application.dto;

import java.util.List;

public record SendPushToUserCommand(
        Long userId,
        PushMessageCommand message
) {
}
