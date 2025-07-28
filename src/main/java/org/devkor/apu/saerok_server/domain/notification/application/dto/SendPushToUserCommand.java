package org.devkor.apu.saerok_server.domain.notification.application.dto;

public record SendPushToUserCommand(
        Long userId,
        PushMessageCommand message
) {
}
