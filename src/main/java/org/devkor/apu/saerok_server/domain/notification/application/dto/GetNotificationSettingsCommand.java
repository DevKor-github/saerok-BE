package org.devkor.apu.saerok_server.domain.notification.application.dto;

public record GetNotificationSettingsCommand(
        Long userId,
        String deviceId
) {
}
